package com.example.fridgehelper.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromProductStatus(status: ProductStatus): String = status.name

    @TypeConverter
    fun toProductStatus(value: String): ProductStatus =
        ProductStatus.valueOf(value)
}
