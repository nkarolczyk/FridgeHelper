package com.example.fridgehelper.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpoonacularApi {

    // szuka przepisów po liście składników
    // składniki po angielsku!!!
    @GET("recipes/findByIngredients")
    suspend fun findByIngredients(
        @Query("ingredients") ingredients: String,
        @Query("number") number: Int = 10,
        @Query("ranking") ranking: Int = 1,
        @Query("ignorePantry") ignorePantry: Boolean = true,
        @Query("apiKey") apiKey: String
    ): List<RecipeDto>

    // szczegóły przepisu (kroki, czas) JESZCZE NIE UZYWANE W UI
    @GET("recipes/{id}/information")
    suspend fun getRecipeDetail(
        @Path("id") id: Int,
        @Query("apiKey") apiKey: String
    ): RecipeDetailDto

    // autocomplete nazw składników używane w skanerze do mapowania nazwy produktu
    @GET("food/ingredients/autocomplete")
    suspend fun autocompleteIngredient(
        @Query("query") query: String,
        @Query("number") number: Int = 5,
        @Query("apiKey") apiKey: String
    ): List<IngredientSuggestionDto>
}