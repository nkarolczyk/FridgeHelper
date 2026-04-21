package com.example.fridgehelper.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// jeden wiersz w tabeli products w bazie room
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) // id automatycznie przez bazę
    val id: Int = 0,
    val name: String,
    val barcode: String? = null,    // null gdy produkt dodany ręcznie bez skanowania
    val quantity: Int = 1,
    val unit: String = "szt",
    val expiryDate: Long,           // timestamp w milisekundach
    val addedDate: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,

    // wartości odżywcze na 100g — null gdy nieznane (brak skanu lub brak danych w api)
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null
)