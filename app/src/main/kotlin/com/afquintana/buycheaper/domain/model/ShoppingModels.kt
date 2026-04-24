package com.afquintana.buycheaper.domain.model

data class Supermarket(
    val id: String,
    val name: String,
    val colorHex: String
)

data class Section(
    val id: String,
    val title: String
)

data class Product(
    val id: String,
    val name: String,
    val supermarketId: String,
    val sectionId: String,
    val price: Double,
    val quantity: Double
) {
    val total: Double = price * quantity
}

sealed interface ShoppingItem {
    data class SectionItem(val section: Section) : ShoppingItem
    data class ProductItem(val product: Product) : ShoppingItem
}
