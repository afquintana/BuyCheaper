package com.afquintana.buycheaper.data.repository

import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.Section
import com.afquintana.buycheaper.domain.model.Supermarket
import com.afquintana.buycheaper.domain.repository.ShoppingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FirestoreShoppingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ShoppingRepository {

    private val sectionsCollection get() = firestore.collection("sections")
    private val productsCollection get() = firestore.collection("products")
    private val supermarketsCollection get() = firestore.collection("supermarkets")

    override fun observeSections(): Flow<List<Section>> = callbackFlow {
        val registration = sectionsCollection.addSnapshotListener { snapshot, _ ->
            val sections = snapshot?.documents.orEmpty().map { doc ->
                Section(
                    id = doc.id,
                    title = doc.getString("title").orEmpty()
                )
            }
            trySend(sections)
        }
        awaitClose { registration.remove() }
    }

    override fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val registration = productsCollection.addSnapshotListener { snapshot, _ ->
            val products = snapshot?.documents.orEmpty().map { doc ->
                Product(
                    id = doc.id,
                    name = doc.getString("name").orEmpty(),
                    supermarketId = doc.getString("supermarketId").orEmpty(),
                    sectionId = doc.getString("sectionId").orEmpty(),
                    price = doc.getDouble("price") ?: 0.0,
                    quantity = doc.getDouble("quantity") ?: 0.0
                )
            }
            trySend(products)
        }
        awaitClose { registration.remove() }
    }

    override fun observeSupermarkets(): Flow<List<Supermarket>> = callbackFlow {
        val registration = supermarketsCollection.addSnapshotListener { snapshot, _ ->
            val supermarkets = snapshot?.documents.orEmpty().map { doc ->
                Supermarket(
                    id = doc.id,
                    name = doc.getString("name").orEmpty(),
                    colorHex = doc.getString("colorHex").orEmpty()
                )
            }
            trySend(supermarkets)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun addSection(title: String) {
        sectionsCollection.document(UUID.randomUUID().toString()).set(mapOf("title" to title)).await()
    }

    override suspend fun deleteSection(sectionId: String) {
        sectionsCollection.document(sectionId).delete().await()
    }

    override suspend fun addProduct(product: Product) {
        val id = product.id.ifEmpty { UUID.randomUUID().toString() }
        productsCollection.document(id).set(product.toMap()).await()
    }

    override suspend fun updateProduct(product: Product) {
        productsCollection.document(product.id).set(product.toMap()).await()
    }

    override suspend fun deleteProduct(productId: String) {
        productsCollection.document(productId).delete().await()
    }

    override suspend fun getProductById(id: String): Product? {
        val doc = productsCollection.document(id).get().await()
        if (!doc.exists()) return null
        return Product(
            id = doc.id,
            name = doc.getString("name").orEmpty(),
            supermarketId = doc.getString("supermarketId").orEmpty(),
            sectionId = doc.getString("sectionId").orEmpty(),
            price = doc.getDouble("price") ?: 0.0,
            quantity = doc.getDouble("quantity") ?: 0.0
        )
    }

    override suspend fun addSupermarket(supermarket: Supermarket) {
        val id = supermarket.id.ifEmpty { UUID.randomUUID().toString() }
        supermarketsCollection.document(id).set(
            mapOf(
                "name" to supermarket.name,
                "colorHex" to supermarket.colorHex
            )
        ).await()
    }

    private fun Product.toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "supermarketId" to supermarketId,
        "sectionId" to sectionId,
        "price" to price,
        "quantity" to quantity
    )
}
