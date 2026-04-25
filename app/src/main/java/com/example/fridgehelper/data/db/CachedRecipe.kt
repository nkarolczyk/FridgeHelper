package com.example.fridgehelper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_recipes")
data class CachedRecipe(
    @PrimaryKey
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val usedIngredients: Int,
    val missedIngredients: Int,
    val cachedAt: Long = System.currentTimeMillis(),
    val ingredientsKey: String = ""
)