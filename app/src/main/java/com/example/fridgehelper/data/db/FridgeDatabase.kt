package com.example.fridgehelper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Product::class, CachedRecipe::class, CachedRecipeDetail::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FridgeDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cachedRecipeDao(): CachedRecipeDao
    abstract fun cachedRecipeDetailDao(): CachedRecipeDetailDao
}
