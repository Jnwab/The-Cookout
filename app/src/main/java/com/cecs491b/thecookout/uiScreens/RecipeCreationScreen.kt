@file:OptIn(ExperimentalMaterial3Api::class)

package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Let's create your recipe!",
            style = MaterialTheme.typography.headlineMedium
        )
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