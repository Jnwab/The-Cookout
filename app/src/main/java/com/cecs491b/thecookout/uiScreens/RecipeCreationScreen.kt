@file:OptIn(ExperimentalMaterial3Api::class)

package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import androidx.compose.ui.tooling.preview.Preview

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import com.cecs491b.thecookout.viewmodels.RecipeCreationViewModel


@Composable
fun RecipeCreationScreen() {
    val navController = rememberNavController()
    val viewModel: RecipeCreationViewModel = viewModel()

    Scaffold(
        topBar = {
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
            TopAppBar(
                title = { Text(getTitleForRoute(currentDestination)) },
                navigationIcon = {
                    if (currentDestination != "main") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                {
                    Button(
                        onClick = {
                            // TODO: send payload of data here into database (can make a function in viewmodel)
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                    ) {
                        Text("Publish Recipe")
                    }
                }
            )
        },
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

private fun getTitleForRoute(route: String?): String {
    return when (route) {
        "main" -> "Create a Recipe"
        "ingredients" -> "Add Ingredients"
        "steps" -> "Add Steps"
        else -> ""
    }
}


@Composable
fun RecipeCreationMainScreen(navController: NavHostController, viewModel: RecipeCreationViewModel) {

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        viewModel.photoUri = uri
    }


    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Recipe Photo",
            style = MaterialTheme.typography.headlineSmall
        )
        if (viewModel.photoUri != null) {
            AsyncImage(
                model = viewModel.photoUri,
                contentDescription = "Selected recipe image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.photoUri = null },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Remove Photo") }
        } else {
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Photo")
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = "Recipe Title",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.title = it },
            label = { Text("e.g., Classic Carbonara") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(5.dp))
        Text(
            text = "Description",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = viewModel.description,
            onValueChange = { viewModel.description = it },
            label = { Text("Describe your recipe...") },
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )

        Spacer(Modifier.height(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 5.dp).weight(1f)
            ) {
                Text(
                    text = "Prep Time (mins)",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = viewModel.preptime,
                    onValueChange = { viewModel.preptime = it },
                    label = { Text("15") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Servings",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = viewModel.servings,
                    onValueChange = { viewModel.servings = it },
                    label = { Text("4") },
                    singleLine = true,
                )
            }

            Spacer(Modifier.width(10.dp))
            
            Column(
                modifier = Modifier.padding(vertical = 5.dp).weight(1f)
            ) {
                Text(
                    text = "Cook Time (mins)",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = viewModel.cooktime,
                    onValueChange = { viewModel.cooktime = it },
                    label = { Text("25") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = viewModel.difficulty,
                    onValueChange = { viewModel.difficulty = it },
                    label = { Text("Hard") },
                    singleLine = true,
                )
            }
        }

        

        Spacer(Modifier.height(24.dp))

        Text(text = "Ingredients", style = MaterialTheme.typography.titleSmall)
        if (viewModel.ingredients.isEmpty()) {
            Text("No ingredients yet", style = MaterialTheme.typography.bodySmall)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                viewModel.ingredients.take(3).forEachIndexed { i, ing ->
                    Text("${i + 1}. $ing", style = MaterialTheme.typography.bodySmall)
                }
                if (viewModel.ingredients.size > 3) {
                    Text("…${viewModel.ingredients.size - 3} more", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Button(
            onClick = { navController.navigate("ingredients") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Ingredients")
        }

        Spacer(Modifier.height(16.dp))

        Text(text = "Steps", style = MaterialTheme.typography.titleSmall)
        if (viewModel.steps.isEmpty()) {
            Text("No steps yet", style = MaterialTheme.typography.bodySmall)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                viewModel.steps.take(3).forEachIndexed { i, ing ->
                    Text("${i + 1}. $ing", style = MaterialTheme.typography.bodySmall)
                }
                if (viewModel.steps.size > 3) {
                    Text("…${viewModel.steps.size - 3} more", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Button(
            onClick = { navController.navigate("steps") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Steps")
        }
    }
}

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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add to List")
        }
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
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }

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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Step")
        }
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecipeCreationScreenPreview() {
    TheCookoutTheme(darkTheme = false, dynamicColor = false) {
        RecipeCreationScreen()
    }
}