package com.cecs491b.thecookout.activities

import android.content.Intent
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
import com.cecs491b.thecookout.uiScreens.RecipeDetailScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipeDetailActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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

                    val currentUserId = auth.currentUser?.uid
                    val isOwner = recipe?.authorId == currentUserId

                    LaunchedEffect(recipeId) {
                        loadRecipe(recipeId) { loadedRecipe ->
                            recipe = loadedRecipe
                            isLoading = false
                        }
                    }

                    RecipeDetailScreen(
                        recipe = recipe,
                        isLoading = isLoading,
                        isOwner = isOwner,
                        onBackClick = { finish() },
                        onEditClick = {
                            val intent = Intent(this@RecipeDetailActivity, RecipeEditActivity::class.java)
                            intent.putExtra("recipeId", recipeId)
                            startActivity(intent)
                        },
                        onDeleteClick = {
                            recipe?.let { deleteRecipe(it.id) }
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

    private fun deleteRecipe(recipeId: String) {
        db.collection("recipes").document(recipeId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Recipe deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete recipe", Toast.LENGTH_SHORT).show()
            }
    }
}