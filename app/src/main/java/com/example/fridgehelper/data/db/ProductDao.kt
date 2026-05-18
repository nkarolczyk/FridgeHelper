package com.example.fridgehelper.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// statystyka
data class MonthStat(val month: String, val count: Int)

//DODWAANIE USUWANIE UPDATE PRODUKTU
@Dao
interface ProductDao {
    // aktywna lista lodówki= tylko produkty nadal w
    @Query("SELECT * FROM products WHERE status = 'IN_FRIDGE' ORDER BY expiryDate ASC")
    fun getAllActive(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE expiryDate < :threshold AND status = 'IN_FRIDGE'")
    suspend fun getExpiringSoon(threshold: Long): List<Product>

    // ostatnio zeskanowane produkty (ean) do paska co bylo zeskanowane
    @Query("SELECT * FROM products WHERE barcode IS NOT NULL AND status = 'IN_FRIDGE' ORDER BY addedDate DESC LIMIT :limit")
    fun getRecentlyScanned(limit: Int): Flow<List<Product>>

    // statystyki miesięczne= produkty oznaczone jako zużyte (Flow = odświeża się przy każdej zmianie w tabeli)
    @Query("""
        SELECT strftime('%Y-%m', datetime(resolvedDate / 1000, 'unixepoch')) AS month,
               COUNT(*) AS count
        FROM products
        WHERE status = 'USED' AND resolvedDate IS NOT NULL
        GROUP BY month
        ORDER BY month ASC
    """)
    fun getMonthlyUsed(): Flow<List<MonthStat>>

    // statystyki miesięczne =produkty wyrzucone wasted
    @Query("""
        SELECT strftime('%Y-%m', datetime(resolvedDate / 1000, 'unixepoch')) AS month,
               COUNT(*) AS count
        FROM products
        WHERE status = 'WASTED' AND resolvedDate IS NOT NULL
        GROUP BY month
        ORDER BY month ASC
    """)
    fun getMonthlyWasted(): Flow<List<MonthStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Update
    suspend fun update(product: Product)
}
