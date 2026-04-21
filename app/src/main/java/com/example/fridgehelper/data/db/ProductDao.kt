package com.example.fridgehelper.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
//DODWAANIE USUWANIE UPDATE PRODUKTU
@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY expiryDate ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE expiryDate < :threshold")
    suspend fun getExpiringSoon(threshold: Long): List<Product>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Update
    suspend fun update(product: Product)
}