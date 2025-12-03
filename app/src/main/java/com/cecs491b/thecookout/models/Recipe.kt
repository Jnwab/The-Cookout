package com.cecs491b.thecookout.models

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val prepTime: Int = 0,
    val cookTime: Int = 0,
    val servings: Int = 0,
    val difficulty: String = "",
    val category: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val photoUrl: String? = null,
    val authorId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
