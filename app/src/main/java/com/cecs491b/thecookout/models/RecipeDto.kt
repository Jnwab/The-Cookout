package com.cecs491b.thecookout.models

data class IngredientDto(
    val name: String,
    val quantity: String?,
    val unit: String?,
    val notes: String?
)

data class RecipeDto(
    val name: String,
    val description: String,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,
    val servings: Int?,
    val difficulty: String?, // "easy" | "medium" | "hard" or null
    val ingredients: List<IngredientDto>,
    val steps: List<String>
)