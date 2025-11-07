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
                        onClick = { },
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
            composable("main") { RecipeCreationMainScreen(navController) }
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
fun RecipeCreationMainScreen(navController: NavHostController) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var preptime by rememberSaveable { mutableStateOf("") }
    var cooktime by rememberSaveable { mutableStateOf("") }
    var servings by rememberSaveable { mutableStateOf("") }
    // make these two enumerable
    var difficulty by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
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
        Button(
            onClick = { // photo picker window
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Photo")
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = "Recipe Title",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
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
            value = description,
            onValueChange = { description = it },
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
                    value = preptime,
                    onValueChange = { preptime = it },
                    label = { Text("15") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Servings",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
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
                    value = cooktime,
                    onValueChange = { cooktime = it },
                    label = { Text("25") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Difficulty",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    label = { Text("Hard") },
                    singleLine = true,
                )
            }
        }

        

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("ingredients") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Ingredients")
        }
        Spacer(Modifier.height(16.dp))
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