package com.afquintana.buycheaper.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.Supermarket
import com.afquintana.buycheaper.domain.usecase.GetProductByIdUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveSupermarketsUseCase
import com.afquintana.buycheaper.domain.usecase.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    observeSupermarketsUseCase: ObserveSupermarketsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeSupermarketsUseCase().collect { markets ->
                _state.update { it.copy(supermarkets = markets) }
            }
        }
    }

    fun load(productId: String) {
        viewModelScope.launch {
            val product = getProductByIdUseCase(productId) ?: return@launch
            _state.value = _state.value.copy(
                productId = product.id,
                name = product.name,
                supermarketId = product.supermarketId,
                price = product.price.toString(),
                quantity = product.quantity.toString()
            )
        }
    }

    fun onNameChanged(value: String) = _state.update { it.copy(name = value) }
    fun onSupermarketChanged(value: String) = _state.update { it.copy(supermarketId = value) }
    fun onPriceChanged(value: String) = _state.update { it.copy(price = value) }
    fun onQuantityChanged(value: String) = _state.update { it.copy(quantity = value) }

    fun save() {
        viewModelScope.launch {
            val current = state.value
            val product = Product(
                id = current.productId,
                name = current.name,
                supermarketId = current.supermarketId,
                price = current.price.toDoubleOrNull() ?: 0.0,
                quantity = current.quantity.toDoubleOrNull() ?: 0.0
            )
            runCatching { updateProductUseCase(product) }
                .onSuccess { _state.update { it.copy(saved = true) } }
                .onFailure { throwable ->
                    _state.update { currentState -> currentState.copy(error = throwable.message) }
                }
        }
    }
}

data class ProductDetailUiState(
    val productId: String = "",
    val name: String = "",
    val supermarketId: String = "",
    val price: String = "",
    val quantity: String = "",
    val supermarkets: List<Supermarket> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null
)
