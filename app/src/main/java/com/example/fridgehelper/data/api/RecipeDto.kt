package com.example.fridgehelper.data.api

import com.google.gson.annotations.SerializedName

// skrócone dane przepisu
data class RecipeDto(
    val id: Int,
    val title: String,

    @SerializedName("image")
    val imageUrl: String?,

    //skladniki pasujace
    @SerializedName("usedIngredientCount")
    val usedIngredients: Int,
    //skladniki brakujace
    @SerializedName("missedIngredientCount")
    val missedIngredients: Int
)

// pełne dane przepisu — używane przez RecipeDetailScreen
data class RecipeDetailDto(
    val id: Int,
    val title: String,

    @SerializedName("image")
    val imageUrl: String?,

    val readyInMinutes: Int?,
    val servings: Int?,
    val summary: String?,

    @SerializedName("analyzedInstructions")
    val instructions: List<InstructionDto>?,

    @SerializedName("extendedIngredients")
    val ingredients: List<ExtendedIngredientDto>?
)

// lista kroków dla jednej sekcji przepisu
data class InstructionDto(
    val steps: List<StepDto>?
)
// pojedynczy krok z numerem i opisem
data class StepDto(
    val number: Int,
    val step: String
)

// pojedynczy składnik przepisu ze szczegółami
data class ExtendedIngredientDto(
    val name: String,
    val amount: Double,
    val unit: String
)

// sugestia składnika zwracana przez Spoonacular
data class IngredientSuggestionDto(
    val id: Int,
    val name: String
)