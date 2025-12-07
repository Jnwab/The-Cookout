package com.cecs491b.thecookout.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // IMPORTANT: replace with your current ngrok https URL, MUST end with '/'
    private const val BASE_URL = "https://aware-deerfly-juanita.ngrok-free.dev/"

    val api: RecipeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RecipeApiService::class.java)
    }
}
