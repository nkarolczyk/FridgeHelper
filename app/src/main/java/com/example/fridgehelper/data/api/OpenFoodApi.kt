package com.example.fridgehelper.data.api

import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodApi {
    // pobiera dane produktu z bazy open food facts po kodzie kreskowym ean
    // lc=en wymusza angielski
    @GET("api/v2/product/{barcode}.json?lc=en")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodResponse
}