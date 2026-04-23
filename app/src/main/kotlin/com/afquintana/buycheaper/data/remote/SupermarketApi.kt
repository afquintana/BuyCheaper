package com.afquintana.buycheaper.data.remote

import retrofit2.http.GET

interface SupermarketApi {
    @GET("supermarkets")
    suspend fun getSupermarketCatalog(): List<SupermarketDto>
}

data class SupermarketDto(
    val id: String,
    val name: String,
    val colorHex: String
)
