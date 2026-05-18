package com.example.fridgehelper.data.repository

import com.example.fridgehelper.data.db.MonthStat
import com.example.fridgehelper.data.db.Product
import com.example.fridgehelper.data.db.ProductDao
import com.example.fridgehelper.data.db.ProductStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FridgeRepository @Inject constructor(
    private val dao: ProductDao
) {
    // aktywna lista — tylko produkty IN_FRIDGE (używane przez FridgeViewModel i RecipesViewModel)
    val allProducts: Flow<List<Product>> = dao.getAllActive()

    suspend fun addProduct(product: Product) = dao.insert(product)

    suspend fun removeProduct(product: Product) = dao.delete(product)

    suspend fun updateProduct(product: Product) = dao.update(product)

    // oznacza produkt jako zużyty lub wyrzucony zamiast go kasować
    suspend fun markProduct(product: Product, status: ProductStatus) =
        dao.update(product.copy(status = status, resolvedDate = System.currentTimeMillis()))

    suspend fun getExpiringSoon(daysAhead: Int = 3): List<Product> {
        val threshold = System.currentTimeMillis() +
                daysAhead * 24L * 60 * 60 * 1000
        return dao.getExpiringSoon(threshold)
    }

    // 5 ostatnio zeskanowanych produktów — do paska na ekranie skanera
    fun recentlyScanned(limit: Int = 5): Flow<List<Product>> = dao.getRecentlyScanned(limit)

    fun getMonthlyUsed(): Flow<List<MonthStat>> = dao.getMonthlyUsed()
    fun getMonthlyWasted(): Flow<List<MonthStat>> = dao.getMonthlyWasted()
}
