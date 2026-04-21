package com.example.fridgehelper.data.repository

import com.example.fridgehelper.data.db.Product
import com.example.fridgehelper.data.db.ProductDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FridgeRepository @Inject constructor(
    private val dao: ProductDao
) {
    val allProducts: Flow<List<Product>> = dao.getAllProducts()

    suspend fun addProduct(product: Product) = dao.insert(product)

    suspend fun removeProduct(product: Product) = dao.delete(product)

    suspend fun updateProduct(product: Product) = dao.update(product)

    suspend fun getExpiringSoon(daysAhead: Int = 3): List<Product> {
        val threshold = System.currentTimeMillis() +
                daysAhead * 24L * 60 * 60 * 1000
        return dao.getExpiringSoon(threshold)
    }
}