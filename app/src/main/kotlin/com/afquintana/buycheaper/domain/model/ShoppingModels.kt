package com.afquintana.buycheaper.domain.model

import java.util.Locale

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
    val checkCount: Int = 0,
    val price: Double,
    val quantity: Double,
    val quantityInput: String = "",
    val quantityUnit: QuantityUnit = QuantityUnit.UNIT,
    val currency: CurrencyUnit = CurrencyUnit.EUR
) {
    val total: Double = price
    val isChecked: Boolean get() = checkCount > 0
}

enum class QuantityUnit(
    val storageValue: String,
    val displayLabel: String,
    val normalizedLabel: String
) {
    LITER("L", "L", "L"),
    MILLILITER("ML", "ml", "L"),
    KILOGRAM("KG", "kg", "kg"),
    GRAM("G", "g", "kg"),
    UNIT("UNIT", "ud", "ud");

    companion object {
        fun fromStorage(value: String?): QuantityUnit =
            entries.firstOrNull { it.storageValue == value } ?: UNIT
    }
}

enum class CurrencyUnit(
    val storageValue: String,
    val displayLabel: String
) {
    EUR("EUR", "EUR"),
    USD("USD", "USD"),
    GBP("GBP", "GBP"),
    JPY("JPY", "JPY"),
    CNY("CNY", "CNY"),
    INR("INR", "INR"),
    BRL("BRL", "BRL"),
    MXN("MXN", "MXN");

    companion object {
        fun fromStorage(value: String?): CurrencyUnit =
            entries.firstOrNull { it.storageValue == value } ?: EUR
    }
}

fun Product.quantityDisplayText(): String =
    "${displayQuantityInput(quantityInput, quantity)} ${quantityUnit.displayLabel}"

fun Product.priceDisplayText(): String =
    "${formatDecimal(price, 2)} ${currency.displayLabel}"

fun Product.pricePerQuantityDisplayText(): String =
    pricePerQuantityDisplayText(price, quantity, quantityUnit, currency)

fun pricePerQuantityDisplayText(
    price: Double,
    quantity: Double,
    quantityUnit: QuantityUnit,
    currency: CurrencyUnit
): String {
    val normalizedQuantity = normalizedQuantity(quantity, quantityUnit) ?: return ""
    val referencePrice = price / normalizedQuantity
    return "${formatDecimal(referencePrice, 3)} ${currency.displayLabel}/${quantityUnit.normalizedLabel}"
}

fun normalizedQuantity(
    quantity: Double,
    quantityUnit: QuantityUnit
): Double? {
    if (quantity <= 0.0) return null
    return when (quantityUnit) {
        QuantityUnit.LITER -> quantity
        QuantityUnit.MILLILITER -> quantity / 1_000.0
        QuantityUnit.KILOGRAM -> quantity
        QuantityUnit.GRAM -> quantity / 1_000.0
        QuantityUnit.UNIT -> quantity
    }.takeIf { it > 0.0 }
}

fun parseQuantityInput(rawValue: String): Double? {
    val normalized = rawValue
        .trim()
        .lowercase()
        .replace('×', 'x')
        .replace(',', '.')
        .replace(" ", "")

    if (normalized.isBlank()) return null

    return normalized
        .split('x')
        .takeIf { it.isNotEmpty() }
        ?.map { it.toDoubleOrNull() ?: return null }
        ?.reduce { acc, value -> acc * value }
}

fun formatQuantity(quantity: Double): String =
    if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        formatDecimal(quantity, 3).trimEnd('0').trimEnd('.').replace('.', ',')
    }

fun formatDecimal(value: Double, decimals: Int): String =
    String.format(Locale.US, "%.${decimals}f", value).replace('.', ',')

fun displayQuantityInput(rawValue: String, quantity: Double): String =
    rawValue.trim().ifBlank { formatQuantity(quantity) }

sealed interface ShoppingItem {
    data class SectionItem(val section: Section) : ShoppingItem
    data class ProductItem(val product: Product) : ShoppingItem
}
