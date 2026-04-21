package com.example.fridgehelper.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularApi {

    // szuka przepisów na podstawie listy składników z lodówki
    // lista składników musi byc po angielsku aby to działało!!!!
    @GET("recipes/findByIngredients")
    suspend fun findByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 10,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true,
        @Query("apiKey") apiKey: String
    ): List<RecipeDto>

    // szczegóły konkretnego przepisu
    @GET("recipes/{id}/information")
    suspend fun getRecipeDetail(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String
    ): RecipeDetailDto
}