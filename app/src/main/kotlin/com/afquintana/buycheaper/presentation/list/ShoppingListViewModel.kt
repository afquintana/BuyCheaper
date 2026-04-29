package com.afquintana.buycheaper.presentation.list

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afquintana.buycheaper.domain.model.CurrencyUnit
import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.QuantityUnit
import com.afquintana.buycheaper.domain.model.Section
import com.afquintana.buycheaper.domain.model.Supermarket
import com.afquintana.buycheaper.domain.usecase.AddProductUseCase
import com.afquintana.buycheaper.domain.usecase.AddSectionUseCase
import com.afquintana.buycheaper.domain.usecase.AddSupermarketUseCase
import com.afquintana.buycheaper.domain.usecase.DeleteProductUseCase
import com.afquintana.buycheaper.domain.usecase.DeleteSectionUseCase
import com.afquintana.buycheaper.domain.usecase.DeleteSupermarketUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveProductsUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveSectionsUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveSupermarketsUseCase
import com.afquintana.buycheaper.domain.usecase.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    observeSectionsUseCase: ObserveSectionsUseCase,
    observeProductsUseCase: ObserveProductsUseCase,
    observeSupermarketsUseCase: ObserveSupermarketsUseCase,
    private val sharedPreferences: SharedPreferences,
    private val addSectionUseCase: AddSectionUseCase,
    private val deleteSectionUseCase: DeleteSectionUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val addSupermarketUseCase: AddSupermarketUseCase,
    private val deleteSupermarketUseCase: DeleteSupermarketUseCase
) : ViewModel() {

    private val preferredCurrencyFlow = MutableStateFlow(loadPreferredCurrency())
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val state = combine(
        observeSectionsUseCase(),
        observeProductsUseCase(),
        observeSupermarketsUseCase(),
        preferredCurrencyFlow
    ) { sections, products, supermarkets, preferredCurrency ->
        ShoppingListUiState(
            sections = sections,
            products = products,
            supermarkets = supermarkets,
            grandTotal = products.sumOf { it.total },
            preferredCurrency = preferredCurrency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShoppingListUiState()
    )

    fun addSection(title: String) = viewModelScope.launch {
        runCatching { addSectionUseCase(title.trim().uppercase()) }
            .onFailure { _message.value = it.message }
    }

    fun deleteSection(id: String) = viewModelScope.launch {
        runCatching { deleteSectionUseCase(id) }
            .onFailure { _message.value = it.message }
    }

    fun addSupermarket(name: String, colorHex: String) = viewModelScope.launch {
        val supermarket = Supermarket(
            id = UUID.randomUUID().toString(),
            name = name.trim().uppercase(),
            colorHex = colorHex
        )
        runCatching { addSupermarketUseCase(supermarket) }
            .onFailure { _message.value = it.message }
    }

    fun deleteSupermarket(id: String) = viewModelScope.launch {
        runCatching { deleteSupermarketUseCase(id) }
            .onFailure { _message.value = it.message }
    }

    fun addProduct(
        name: String,
        supermarketId: String,
        sectionId: String,
        price: Double,
        quantity: Double,
        quantityInput: String,
        quantityUnit: QuantityUnit,
        currency: CurrencyUnit
    ) = viewModelScope.launch {
        val product = Product(
            id = UUID.randomUUID().toString(),
            name = name.toTitleCaseWords(),
            supermarketId = supermarketId,
            sectionId = sectionId,
            checkCount = 0,
            price = price,
            quantity = quantity,
            quantityInput = quantityInput,
            quantityUnit = quantityUnit,
            currency = currency
        )
        runCatching {
            addProductUseCase(product)
            savePreferredCurrency(currency)
        }
            .onFailure { _message.value = it.message }
    }

    fun setProductCheckCount(product: Product, checkCount: Int) = viewModelScope.launch {
        runCatching { updateProductUseCase(product.copy(checkCount = checkCount.coerceAtLeast(0))) }
            .onFailure { _message.value = it.message }
    }

    fun deleteProduct(id: String) = viewModelScope.launch {
        runCatching { deleteProductUseCase(id) }
            .onFailure { _message.value = it.message }
    }

    private fun loadPreferredCurrency(): CurrencyUnit =
        CurrencyUnit.fromStorage(sharedPreferences.getString(PREFERRED_CURRENCY_KEY, CurrencyUnit.EUR.storageValue))

    private fun savePreferredCurrency(currency: CurrencyUnit) {
        sharedPreferences.edit()
            .putString(PREFERRED_CURRENCY_KEY, currency.storageValue)
            .apply()
        preferredCurrencyFlow.value = currency
    }
}

private fun String.toTitleCaseWords(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }

data class ShoppingListUiState(
    val sections: List<Section> = emptyList(),
    val products: List<Product> = emptyList(),
    val supermarkets: List<Supermarket> = emptyList(),
    val grandTotal: Double = 0.0,
    val preferredCurrency: CurrencyUnit = CurrencyUnit.EUR
)

private const val PREFERRED_CURRENCY_KEY = "preferred_currency"
