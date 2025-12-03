package com.cecs491b.thecookout.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SavedRecipesRepository(private val context: Context){
    private val prefs = context.getSharedPreferences("saved_recipes_prefs", Context.MODE_PRIVATE)
    private val KEY_SAVED_IDS = "saved_recipe_ids"

    private val _savedIds = MutableStateFlow<Set<String>>(loadSavedIds())
    val savedIds: StateFlow<Set<String>> = _savedIds

    private fun loadSavedIds(): Set<String>{
        return prefs.getStringSet(KEY_SAVED_IDS, emptySet()) ?: emptySet()
    }

    fun saveRecipe(recipeId: String){
        val updated = _savedIds.value.toMutableSet()
        updated.add(recipeId)
        prefs.edit().putStringSet(KEY_SAVED_IDS, updated).apply()
        _savedIds.value = updated
    }

    fun unsaveRecipe(recipeId: String){
        val updated = _savedIds.value.toMutableSet()
        updated.remove(recipeId)
        prefs.edit().putStringSet(KEY_SAVED_IDS, updated).apply()
        _savedIds.value = updated
    }

    fun isRecipeSaved(recipeId: String): Boolean{
        return _savedIds.value.contains(recipeId)
    }
}