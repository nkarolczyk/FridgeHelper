package com.example.fridgehelper.data.db

import androidx.room.*

@Dao
interface CachedRecipeDetailDao {
    @Query("SELECT * FROM cached_recipe_details WHERE id = :id")
    suspend fun getById(id: Int): CachedRecipeDetail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: CachedRecipeDetail)

    // usuwa wpisy starsze niż podany timestamp (po każdym udanym pobraniu)
    @Query("DELETE FROM cached_recipe_details WHERE cachedAt < :threshold")
    suspend fun deleteOlderThan(threshold: Long)
}
