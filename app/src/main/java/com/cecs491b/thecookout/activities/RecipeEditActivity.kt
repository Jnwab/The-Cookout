package com.cecs491b.thecookout.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cecs491b.thecookout.models.Recipe
import com.cecs491b.thecookout.ui.theme.TheCookoutTheme
import com.cecs491b.thecookout.uiScreens.RecipeEditScreen
import com.google.firebase.firestore.FirebaseFirestore

class RecipeEditActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recipeId = intent.getStringExtra("recipeId")

        if (recipeId == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            TheCookoutTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var recipe by remember { mutableStateOf<Recipe?>(null) }
                    var isLoading by remember { mutableStateOf(true) }
                    var isSaving by remember { mutableStateOf(false) }

                    LaunchedEffect(recipeId) {
                        loadRecipe(recipeId) { loadedRecipe ->
                            recipe = loadedRecipe
                            isLoading = false
                        }
                    }

                    RecipeEditScreen(
                        recipe = recipe,
                        isLoading = isLoading,
                        isSaving = isSaving,
                        onBackClick = { finish() },
                        onSaveClick = { updatedRecipe ->
                            isSaving = true
                            saveRecipe(updatedRecipe) {
                                isSaving = false
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun loadRecipe(recipeId: String, onComplete: (Recipe?) -> Unit) {
        db.collection("recipes").document(recipeId)
            .get()
            .addOnSuccessListener { document ->
                val recipe = document.toObject(Recipe::class.java)
                onComplete(recipe)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load recipe", Toast.LENGTH_SHORT).show()
                onComplete(null)
            }
    }

    private fun saveRecipe(recipe: Recipe, onComplete: () -> Unit) {
        db.collection("recipes").document(recipe.id)
            .set(recipe)
            .addOnSuccessListener {
                Toast.makeText(this, "Recipe updated!", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }
}