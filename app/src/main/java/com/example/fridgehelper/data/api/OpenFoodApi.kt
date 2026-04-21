package com.example.fridgehelper.data.api

import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodApi {
    /**
     * dane produktu po kodzie EAN
     */
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodResponse
}