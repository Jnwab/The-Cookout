package com.cecs491b.thecookout.uiScreens

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

class RecipeCreationViewModel : ViewModel() {
    val ingredients: SnapshotStateList<String> = mutableStateListOf()
    val steps: SnapshotStateList<String> = mutableStateListOf()

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
}
