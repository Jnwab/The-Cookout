package com.cecs491b.thecookout.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.google.firebase.annotations.PublicApi
import androidx.lifecycle.viewModelScope
import com.cecs491b.thecookout.models.Recipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.StateFlow

sealed class PublishState{
    object Idle: PublishState()
    object Loading: PublishState()
    data class Success(val recipeId: String): PublishState()
    data class Error(val message:String): PublishState()
}

class RecipeCreationViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
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

    private val _publishState = MutableStateFlow<PublishState>(PublishState.Idle)
    val publishState: StateFlow<PublishState> = _publishState


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

    fun publishRecipe(){
        viewModelScope.launch{
            _publishState.value = PublishState.Loading

            try{
                val recipeRef = firestore.collection("recipes").document()
                val recipe = Recipe(
                    id = recipeRef.id,
                    title = title,
                    description = description,
                    prepTime = preptime.toIntOrNull() ?: 0,
                    cookTime = cooktime.toIntOrNull() ?: 0,
                    servings = servings.toIntOrNull() ?: 0,
                    difficulty = difficulty,
                    category = category,
                    ingredients = ingredients.toList(),
                    steps = steps.toList(),
                    photoUrl = null, // will need to add Firestore in future to hold photos
                    authorId = auth.currentUser?.uid ?: "",
                    createdAt = System.currentTimeMillis()
                )
                recipeRef.set(recipe).await()
                _publishState.value = PublishState.Success(recipe.id)
                clearForm()
            } catch (e: Exception){
                _publishState.value = PublishState.Error(e.message ?: "Failed to publish recipe")
            }
        }
    }

    private fun clearForm(){
        title = ""
        description = ""
        preptime = ""
        cooktime = ""
        servings = ""
        category = ""
        photoUri = null
        ingredients.clear()
        steps.clear()
    }

    fun resetPublishState(){
        _publishState.value = PublishState.Idle
    }

}