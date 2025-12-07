package com.cecs491b.thecookout.uiScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cecs491b.thecookout.models.Recipe
import com.cecs491b.thecookout.ui.theme.CookoutOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    recipe: Recipe?,
    isLoading: Boolean,
    isSaving: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: (Recipe) -> Unit
) {
    // Local state for editing
    var title by remember(recipe) { mutableStateOf(recipe?.title ?: "") }
    var description by remember(recipe) { mutableStateOf(recipe?.description ?: "") }
    var prepTime by remember(recipe) { mutableStateOf(recipe?.prepTime?.toString() ?: "") }
    var cookTime by remember(recipe) { mutableStateOf(recipe?.cookTime?.toString() ?: "") }
    var servings by remember(recipe) { mutableStateOf(recipe?.servings?.toString() ?: "") }
    var difficulty by remember(recipe) { mutableStateOf(recipe?.difficulty ?: "") }
    var ingredients by remember(recipe) { mutableStateOf(recipe?.ingredients ?: emptyList()) }
    var steps by remember(recipe) { mutableStateOf(recipe?.steps ?: emptyList()) }

    var newIngredient by remember { mutableStateOf("") }
    var newStep by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = {
                        recipe?.let {
                            val updated = it.copy(
                                title = title,
                                description = description,
                                prepTime = prepTime.toIntOrNull() ?: 0,
                                cookTime = cookTime.toIntOrNull() ?: 0,
                                servings = servings.toIntOrNull() ?: 0,
                                difficulty = difficulty,
                                ingredients = ingredients,
                                steps = steps
                            )
                            onSaveClick(updated)
                        }
                    },
                    enabled = !isSaving && recipe != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (recipe == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Recipe not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Time and servings row
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        label = { Text("Prep (min)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = cookTime,
                        onValueChange = { cookTime = it },
                        label = { Text("Cook (min)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it },
                        label = { Text("Servings") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = { difficulty = it },
                        label = { Text("Difficulty") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Ingredients
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                ingredients.forEachIndexed { index, ingredient ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• $ingredient",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { ingredients = ingredients.toMutableList().apply { removeAt(index) } }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newIngredient,
                        onValueChange = { newIngredient = it },
                        label = { Text("Add ingredient") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (newIngredient.isNotBlank()) {
                                ingredients = ingredients + newIngredient
                                newIngredient = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = CookoutOrange)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Steps
                Text(
                    text = "Steps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. $step",
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { steps = steps.toMutableList().apply { removeAt(index) } }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newStep,
                        onValueChange = { newStep = it },
                        label = { Text("Add step") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (newStep.isNotBlank()) {
                                steps = steps + newStep
                                newStep = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = CookoutOrange)
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}