package com.afquintana.buycheaper.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.Section
import com.afquintana.buycheaper.domain.model.Supermarket
import com.afquintana.buycheaper.domain.usecase.AddProductUseCase
import com.afquintana.buycheaper.domain.usecase.AddSectionUseCase
import com.afquintana.buycheaper.domain.usecase.AddSupermarketUseCase
import com.afquintana.buycheaper.domain.usecase.DeleteProductUseCase
import com.afquintana.buycheaper.domain.usecase.DeleteSectionUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveProductsUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveSectionsUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveSupermarketsUseCase
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
    private val addSectionUseCase: AddSectionUseCase,
    private val deleteSectionUseCase: DeleteSectionUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val addSupermarketUseCase: AddSupermarketUseCase
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val state = combine(
        observeSectionsUseCase(),
        observeProductsUseCase(),
        observeSupermarketsUseCase()
    ) { sections, products, supermarkets ->
        ShoppingListUiState(
            sections = sections,
            products = products,
            supermarkets = supermarkets,
            grandTotal = products.sumOf { it.total }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShoppingListUiState()
    )

    fun addSection(title: String) = viewModelScope.launch {
        runCatching { addSectionUseCase(title) }
            .onFailure { _message.value = it.message }
    }

    fun deleteSection(id: String) = viewModelScope.launch {
        runCatching { deleteSectionUseCase(id) }
            .onFailure { _message.value = it.message }
    }

    fun addSupermarket(name: String, colorHex: String) = viewModelScope.launch {
        val supermarket = Supermarket(id = UUID.randomUUID().toString(), name = name, colorHex = colorHex)
        runCatching { addSupermarketUseCase(supermarket) }
            .onFailure { _message.value = it.message }
    }

    fun addProduct(
        name: String,
        supermarketId: String,
        sectionId: String,
        price: Double,
        quantity: Double
    ) = viewModelScope.launch {
        val product = Product(
            id = UUID.randomUUID().toString(),
            name = name,
            supermarketId = supermarketId,
            sectionId = sectionId,
            price = price,
            quantity = quantity
        )
        runCatching { addProductUseCase(product) }
            .onFailure { _message.value = it.message }
    }

    fun deleteProduct(id: String) = viewModelScope.launch {
        runCatching { deleteProductUseCase(id) }
            .onFailure { _message.value = it.message }
    }
}

data class ShoppingListUiState(
    val sections: List<Section> = emptyList(),
    val products: List<Product> = emptyList(),
    val supermarkets: List<Supermarket> = emptyList(),
    val grandTotal: Double = 0.0
)
