package com.example.fridgehelper.data.api

import com.google.gson.annotations.SerializedName

// wynik z findByIngredients — skrócona informacja o przepisie
data class RecipeDto(
    val id: Int,
    val title: String,

    @SerializedName("image")
    val imageUrl: String?,

    @SerializedName("usedIngredientCount")
    val usedIngredients: Int,

    @SerializedName("missedIngredientCount")
    val missedIngredients: Int
)

// wynik z information — szczegóły przepisu
data class RecipeDetailDto(
    val id: Int,
    val title: String,

    @SerializedName("image")
    val imageUrl: String?,

    val readyInMinutes: Int?,
    val servings: Int?,
    val summary: String?,

    @SerializedName("analyzedInstructions")
    val instructions: List<InstructionDto>?
)

data class InstructionDto(
    val steps: List<StepDto>?
)

data class StepDto(
    val number: Int,
    val step: String
)