package com.cecs491b.thecookout.viewmodels

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel

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