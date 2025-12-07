package com.cecs491b.thecookout.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.cecs491b.thecookout.models.RecipeDto

class RecipeCreationViewModel : ViewModel() {
    val ingredients: SnapshotStateList<String> = mutableStateListOf()
    val steps: SnapshotStateList<String> = mutableStateListOf()

    var isImportingFromVideo by mutableStateOf(false)
    var importErrorMessage by mutableStateOf<String?>(null)


    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var preptime by mutableStateOf("")
    var cooktime by mutableStateOf("")
    var servings by mutableStateOf("")
    // TODO: make these two enumerable
    var difficulty by mutableStateOf("")
    var category by mutableStateOf("")

    var photoUri by mutableStateOf<Uri?>(null)


    fun addIngredient(item: String) {
        if (item.isNotBlank()) ingredients.add(item)
    }

    fun removeIngredient(index: Int) {
        if (index in ingredients.indices) ingredients.removeAt(index)
    }

    fun addStep(step: String) {
        if (step.isNotBlank()) steps.add(step)
    }

    fun removeStep(index: Int) {
        if (index in steps.indices) steps.removeAt(index)
    }

    fun applyParsedRecipe(recipe: RecipeDto) {
        title = recipe.name
        description = recipe.description

        preptime = recipe.prepTimeMinutes?.toString() ?: ""
        cooktime = recipe.cookTimeMinutes?.toString() ?: ""
        servings = recipe.servings?.toString() ?: ""

        difficulty = recipe.difficulty
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?: ""

        // Convert IngredientDto → human-readable strings for your list
        ingredients.clear()
        ingredients.addAll(
            recipe.ingredients.map { ing ->
                buildString {
                    if (!ing.quantity.isNullOrBlank()) append(ing.quantity).append(" ")
                    if (!ing.unit.isNullOrBlank()) append(ing.unit).append(" ")
                    append(ing.name)
                    if (!ing.notes.isNullOrBlank()) append(" (").append(ing.notes).append(")")
                }.trim()
            }
        )

        // Steps: direct copy
        steps.clear()
        steps.addAll(recipe.steps)
    }

}