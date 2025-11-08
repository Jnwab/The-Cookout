package com.cecs491b.thecookout.uiScreens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

import android.net.Uri

class RecipeCreationViewModel : ViewModel() {
    val ingredients: SnapshotStateList<String> = mutableStateListOf()
    val steps: SnapshotStateList<String> = mutableStateListOf()

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
}
