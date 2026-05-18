package com.example.fridgehelper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// cache szczegółów przepisu (używany gdy brak internetu przy otwieraniu RecipeDetailScreen)
@Entity(tableName = "cached_recipe_details")
data class CachedRecipeDetail(
    @PrimaryKey val id: Int,
    val title: String,
    val imageUrl: String?,
    val readyInMinutes: Int?,
    val servings: Int?,
    val summary: String?,
    val instructionsJson: String?,
    val ingredientsJson: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
