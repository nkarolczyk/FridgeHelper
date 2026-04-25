package com.example.fridgehelper.data.api

import com.google.gson.annotations.SerializedName

// klasy mapujące json z open food facts na obiekty kotlinowe

data class OpenFoodResponse(
    val status: Int,        // 1 = znaleziony, 0 = brak
    val product: OpenFoodProduct?
)

data class OpenFoodProduct(
    @SerializedName("product_name")     // angielska nazwa z api
    val productName: String?,

    @SerializedName("product_name_pl")  // polska nazwa z api
    val productNamePl: String?,

    @SerializedName("image_front_small_url")
    val imageUrl: String?,

    val nutriments: Nutriments?
) {
    fun bestName(): String? = productName?.takeIf { it.isNotBlank() }
        ?: productNamePl?.takeIf { it.isNotBlank() }
}

data class Nutriments(
    @SerializedName("energy-kcal_100g")
    val caloriesPer100g: Double?,

    @SerializedName("proteins_100g")
    val proteinPer100g: Double?,

    @SerializedName("fat_100g")
    val fatPer100g: Double?,

    @SerializedName("carbohydrates_100g")
    val carbsPer100g: Double?
)
