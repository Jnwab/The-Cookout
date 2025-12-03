package com.cecs491b.thecookout.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cecs491b.thecookout.repository.SavedRecipesRepository
import com.cecs491b.thecookout.uiScreens.Recipe
import com.cecs491b.thecookout.uiScreens.demoRecipes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SavedRecipesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SavedRecipesRepository(application.applicationContext)

    val savedIds: StateFlow<Set<String>> = repository.savedIds

    private val _savedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val savedRecipes: StateFlow<List<Recipe>> = _savedRecipes

    init{
        viewModelScope.launch {
            repository.savedIds.collect { ids ->
                _savedRecipes.value = demoRecipes.filter {it.id in ids}
            }
        }
    }

    fun toggleSave(recipeId: String){
        if (repository.isRecipeSaved(recipeId)){
            repository.unsaveRecipe(recipeId)
        } else {
            repository.saveRecipe(recipeId)
        }
    }

    fun isRecipeSaved(recipeId: String): Boolean{
        return repository.isRecipeSaved(recipeId)
    }
}