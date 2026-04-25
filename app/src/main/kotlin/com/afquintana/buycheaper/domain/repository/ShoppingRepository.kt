package com.afquintana.buycheaper.domain.repository

import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.Section
import com.afquintana.buycheaper.domain.model.Supermarket
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    fun observeSections(): Flow<List<Section>>
    fun observeProducts(): Flow<List<Product>>
    fun observeSupermarkets(): Flow<List<Supermarket>>

    suspend fun addSection(title: String)
    suspend fun deleteSection(sectionId: String)

    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(productId: String)
    suspend fun getProductById(id: String): Product?

    suspend fun addSupermarket(supermarket: Supermarket)
    suspend fun deleteSupermarket(supermarketId: String)
}
