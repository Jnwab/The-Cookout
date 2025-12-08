@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.cecs491b.thecookout.uiScreens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.cecs491b.thecookout.network.RetrofitClient
import com.cecs491b.thecookout.network.TikTokRequest
import com.cecs491b.thecookout.ui.theme.CookoutOrange
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.viewmodels.PublishState
import com.cecs491b.thecookout.viewmodels.RecipeCreationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/* ---------------------------------------------------------- */
/*  Root screen with top app bar + sticky Publish button      */
/* ---------------------------------------------------------- */

@Composable
fun RecipeCreationScreen() {
    val navController = rememberNavController()
    val viewModel: RecipeCreationViewModel = viewModel()
    val publishState by viewModel.publishState.collectAsState()
    val context = LocalContext.current

    // Listen for publish result
    LaunchedEffect(publishState) {
        when (val state = publishState) {
            is PublishState.Success -> {
                Toast.makeText(context, "Recipe published!", Toast.LENGTH_SHORT).show()
                viewModel.resetPublishState()
                // TODO: navigate back to Home if you want
            }

            is PublishState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                viewModel.resetPublishState()
            }

            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            val route = navController.currentBackStackEntryAsState().value?.destination?.route
            TopAppBar(
                title = { Text(getTitleForRoute(route)) },
                navigationIcon = {
                    if (route != "main") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                Button(
                    onClick = { viewModel.publishRecipe() },
                    enabled = publishState !is PublishState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CookoutOrange,
                        contentColor = Color.White
                    )
                ) {
                    if (publishState is PublishState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(22.dp)
                                .padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Publish Recipe")
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable("main") { RecipeCreationMainScreen(navController, viewModel) }
            composable("ingredients") { AddIngredientsScreen(navController, viewModel) }
            composable("steps") { AddStepsScreen(navController, viewModel) }
        }
    }
}

private fun getTitleForRoute(route: String?): String =
    when (route) {
        "main" -> "Create Recipe"
        "ingredients" -> "Add Ingredients"
        "steps" -> "Add Steps"
        else -> ""
    }

/* ---------------------------------------------------------- */
/*  Main form – styled to match your mockups                  */
/* ---------------------------------------------------------- */

@Composable
fun RecipeCreationMainScreen(
    navController: NavHostController,
    viewModel: RecipeCreationViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tiktokUrl by rememberSaveable { mutableStateOf("") }

    // Local-only UI state for category & labels (so we don’t touch your VM)
    val categoryOptions = listOf(
        "Select category",
        "Breakfast",
        "Lunch",
        "Dinner",
        "Dessert",
        "Snack",
        "Appetizer"
    )
    var selectedCategory by rememberSaveable { mutableStateOf(categoryOptions.first()) }

    val labelOptions = listOf(
        "Italian", "Pasta", "Quick & Easy", "Comfort Food",
        "Vegetarian", "Vegan", "Gluten-Free", "Low-Carb",
        "Spicy", "Sweet", "Savory", "Healthy"
    )
    var selectedLabels by rememberSaveable { mutableStateOf(setOf<String>()) }

    /* ----------- Pickers for photo & video ---------- */

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.photoUri = uri
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                viewModel.isImportingFromVideo = true
                viewModel.importErrorMessage = null
                try {
                    val api = RetrofitClient.api
                    val contentResolver = context.contentResolver

                    val mimeType = contentResolver.getType(uri) ?: "video/mp4"
                    val input = contentResolver.openInputStream(uri)
                        ?: throw IllegalStateException("Unable to open video")

                    val bytes = input.use { it.readBytes() }

                    val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData(
                        name = "file",
                        filename = "video.mp4",
                        body = requestBody
                    )

                    val response = withContext(Dispatchers.IO) {
                        api.parseRecipe(filePart)
                    }

                    if (response.isSuccessful) {
                        val recipe = response.body()
                        if (recipe != null) {
                            viewModel.applyParsedRecipe(recipe)
                            Toast.makeText(
                                context,
                                "Recipe imported from video!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            viewModel.importErrorMessage = "Empty response from backend."
                        }
                    } else {
                        viewModel.importErrorMessage =
                            "Backend error ${response.code()}: ${response.errorBody()?.string()}"
                    }
                } catch (e: Exception) {
                    viewModel.importErrorMessage =
                        e.message ?: "Unknown error during import"
                } finally {
                    viewModel.isImportingFromVideo = false
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        /* --------- Recipe photo card (big upload box) --------- */
        Text(
            text = "Recipe Photo",
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, CookoutOrange)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.photoUri != null) {
                    AsyncImage(
                        model = viewModel.photoUri,
                        contentDescription = "Recipe photo",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = CookoutOrange,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Click to upload recipe photo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        /* --------- Autofill helpers (video / TikTok URL) --------- */

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Autofill from cooking video",
                    style = MaterialTheme.typography.labelLarge
                )

                Button(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    enabled = !viewModel.isImportingFromVideo,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CookoutOrange,
                        contentColor = Color.White
                    )
                ) {
                    if (viewModel.isImportingFromVideo) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            color = Color.White
                        )
                        Text("Importing…")
                    } else {
                        Text("Select Video to Autofill")
                    }
                }

                OutlinedTextField(
                    value = tiktokUrl,
                    onValueChange = { tiktokUrl = it },
                    label = { Text("Paste TikTok link here") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (tiktokUrl.isBlank()) {
                            Toast.makeText(
                                context,
                                "Please paste a TikTok URL first",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            scope.launch {
                                viewModel.isImportingFromVideo = true
                                viewModel.importErrorMessage = null
                                try {
                                    val api = RetrofitClient.api
                                    val response = withContext(Dispatchers.IO) {
                                        api.parseRecipeFromTikTok(
                                            TikTokRequest(url = tiktokUrl)
                                        )
                                    }
                                    if (response.isSuccessful) {
                                        val recipe = response.body()
                                        if (recipe != null) {
                                            viewModel.applyParsedRecipe(recipe)
                                            Toast.makeText(
                                                context,
                                                "Recipe imported from TikTok!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            viewModel.importErrorMessage =
                                                "Empty response from backend."
                                        }
                                    } else {
                                        viewModel.importErrorMessage =
                                            "Backend error ${response.code()}: ${
                                                response.errorBody()?.string()
                                            }"
                                    }
                                } catch (e: Exception) {
                                    viewModel.importErrorMessage =
                                        e.message
                                            ?: "Unknown error while importing from TikTok"
                                } finally {
                                    viewModel.isImportingFromVideo = false
                                }
                            }
                        }
                    },
                    enabled = !viewModel.isImportingFromVideo,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CookoutOrange,
                        contentColor = Color.White
                    )
                ) {
                    if (viewModel.isImportingFromVideo) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            color = Color.White
                        )
                        Text("Importing…")
                    } else {
                        Text("Import from URL")
                    }
                }

                viewModel.importErrorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        /* ------------- Basic fields (title + description) ------------- */

        Text("Recipe Title", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.title = it },
            label = { Text("e.g., Classic Carbonara") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Description", style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = { Text("Describe your recipe…") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        /* ------------- Prep / cook, servings / difficulty ------------- */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Prep Time (mins)", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = viewModel.preptime,
                    onValueChange = { viewModel.preptime = it },
                    placeholder = { Text("15") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Cook Time (mins)", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = viewModel.cooktime,
                    onValueChange = { viewModel.cooktime = it },
                    placeholder = { Text("25") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Servings", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = viewModel.servings,
                    onValueChange = { viewModel.servings = it },
                    placeholder = { Text("4") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Difficulty", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = viewModel.difficulty,
                    onValueChange = { viewModel.difficulty = it },
                    placeholder = { Text("Select") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        /* ---------------- Category + labels chips ---------------- */

        Text("Category", style = MaterialTheme.typography.labelLarge)

        var categoryExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                },
                placeholder = { Text("Select category") }
            )

            DropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categoryOptions.drop(1).forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedCategory = option
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Text("Labels", style = MaterialTheme.typography.labelLarge)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            labelOptions.forEach { label ->
                val selected = label in selectedLabels
                FilterChip(
                    selected = selected,
                    onClick = {
                        selectedLabels =
                            if (selected) selectedLabels - label else selectedLabels + label
                    },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CookoutOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        /* ---------------- Ingredients & instructions ---------------- */

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ingredients", style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = { navController.navigate("ingredients") }) {
                Text("+ Add")
            }
        }

        if (viewModel.ingredients.isEmpty()) {
            Text("No ingredients yet", style = MaterialTheme.typography.bodySmall)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                viewModel.ingredients.forEachIndexed { i, ing ->
                    Text("${i + 1}. $ing", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Instructions", style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = { navController.navigate("steps") }) {
                Text("+ Add Step")
            }
        }

        if (viewModel.steps.isEmpty()) {
            Text("No steps yet", style = MaterialTheme.typography.bodySmall)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                viewModel.steps.forEachIndexed { i, step ->
                    Text("${i + 1}. $step", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Spacer so content doesn't hide behind the BottomAppBar
        Spacer(Modifier.height(72.dp))
    }
}

/* ---------------------------------------------------------- */
/*  Ingredients & steps sub-screens                           */
/* ---------------------------------------------------------- */

@Composable
fun AddIngredientsScreen(navController: NavHostController, viewModel: RecipeCreationViewModel) {
    var ingredient by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = ingredient,
            onValueChange = { ingredient = it },
            label = { Text("Add an ingredient") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (ingredient.isNotBlank()) {
                    viewModel.addIngredient(ingredient)
                    ingredient = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookoutOrange,
                contentColor = Color.White
            )
        ) { Text("Add to List") }

        Spacer(Modifier.height(24.dp))

        Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        viewModel.ingredients.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${index + 1}. $item")
                TextButton(onClick = { viewModel.removeIngredient(index) }) {
                    Text("Remove")
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookoutOrange,
                contentColor = Color.White
            )
        ) { Text("Done") }
    }
}

@Composable
fun AddStepsScreen(navController: NavHostController, viewModel: RecipeCreationViewModel) {
    var step by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = step,
            onValueChange = { step = it },
            label = { Text("Add a step") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (step.isNotBlank()) {
                    viewModel.addStep(step)
                    step = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookoutOrange,
                contentColor = Color.White
            )
        ) { Text("Add Step") }

        Spacer(Modifier.height(24.dp))

        Text("Steps:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        viewModel.steps.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${index + 1}. $item")
                TextButton(onClick = { viewModel.removeStep(index) }) {
                    Text("Remove")
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = CookoutOrange,
                contentColor = Color.White
            )
        ) { Text("Done") }
    }
}

/* ---------------------------------------------------------- */
/*  Preview                                                   */
/* ---------------------------------------------------------- */

@Preview(showBackground = true)
@Composable
private fun RecipeCreationScreenPreview() {
    TheCookoutTheme(darkTheme = false, dynamicColor = false) {
        RecipeCreationScreen()
    }
}
