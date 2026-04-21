package com.example.fridgehelper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

//POJEDYNCZY PRODUKT
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val barcode: String? = null,
    val quantity: Int = 1,
    val unit: String = "szt",
    val expiryDate: Long,
    val addedDate: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,

    // wartosci odżywcze na 100g (null jeśli nieznane)
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null
)