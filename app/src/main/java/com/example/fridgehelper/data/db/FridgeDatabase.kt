package com.example.fridgehelper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Product::class, CachedRecipe::class],
    version = 4,
    exportSchema = false
)
abstract class FridgeDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cachedRecipeDao(): CachedRecipeDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN calories REAL")
                db.execSQL("ALTER TABLE products ADD COLUMN protein REAL")
                db.execSQL("ALTER TABLE products ADD COLUMN fat REAL")
                db.execSQL("ALTER TABLE products ADD COLUMN carbs REAL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_recipes ADD COLUMN ingredientsKey TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cached_recipes (
                        id INTEGER PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        imageUrl TEXT,
                        usedIngredients INTEGER NOT NULL,
                        missedIngredients INTEGER NOT NULL,
                        cachedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}