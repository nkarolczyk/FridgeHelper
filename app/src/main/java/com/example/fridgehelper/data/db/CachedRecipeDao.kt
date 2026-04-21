package com.example.fridgehelper.data.db

import androidx.room.*

@Dao
interface CachedRecipeDao {

    @Query("SELECT * FROM cached_recipes ORDER BY usedIngredients DESC")
    suspend fun getAll(): List<CachedRecipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<CachedRecipe>)

    @Query("DELETE FROM cached_recipes")
    suspend fun clearAll()

    @Query("SELECT cachedAt FROM cached_recipes ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getLastCachedAt(): Long?
}