package com.cecs491b.thecookout.network

import com.cecs491b.thecookout.models.RecipeDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class TikTokRequest(val url: String)

interface RecipeApiService {

    @Multipart
    @POST("parse_recipe")
    suspend fun parseRecipe(
        @Part file: MultipartBody.Part
    ): Response<RecipeDto>

    @POST("parse_recipe_tiktok")
    suspend fun parseRecipeFromTikTok(
        @Body body: TikTokRequest
    ): Response<RecipeDto>
}
