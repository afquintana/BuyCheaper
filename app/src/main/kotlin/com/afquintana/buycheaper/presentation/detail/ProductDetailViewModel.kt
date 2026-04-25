package com.afquintana.buycheaper.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.Section
import com.afquintana.buycheaper.domain.model.Supermarket
import com.afquintana.buycheaper.domain.usecase.DeleteProductUseCase
import com.afquintana.buycheaper.domain.usecase.DeleteSectionUseCase
import com.afquintana.buycheaper.domain.usecase.DeleteSupermarketUseCase
import com.afquintana.buycheaper.domain.usecase.GetProductByIdUseCase
import com.afquintana.buycheaper.domain.usecase.ObserveSectionsUseCase
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
    private val deleteProductUseCase: DeleteProductUseCase,
    private val deleteSectionUseCase: DeleteSectionUseCase,
    private val deleteSupermarketUseCase: DeleteSupermarketUseCase,
    observeSectionsUseCase: ObserveSectionsUseCase,
    observeSupermarketsUseCase: ObserveSupermarketsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeSectionsUseCase().collect { sections ->
                _state.update { it.copy(sections = sections) }
            }
        }
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
                sectionId = product.sectionId,
                checked = product.checked,
                price = product.price.toString(),
                quantity = product.quantity.toString()
            )
        }
    }

    fun onNameChanged(value: String) = _state.update { it.copy(name = value) }
    fun onSupermarketChanged(value: String) = _state.update { it.copy(supermarketId = value) }
    fun onSectionChanged(value: String) = _state.update { it.copy(sectionId = value) }
    fun onPriceChanged(value: String) = _state.update { it.copy(price = value) }
    fun onQuantityChanged(value: String) = _state.update { it.copy(quantity = value) }

    fun deleteSection(sectionId: String) {
        viewModelScope.launch {
            runCatching { deleteSectionUseCase(sectionId) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            sectionId = if (it.sectionId == sectionId) "" else it.sectionId
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(error = throwable.message) }
                }
        }
    }

    fun deleteSupermarket(supermarketId: String) {
        viewModelScope.launch {
            runCatching { deleteSupermarketUseCase(supermarketId) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            supermarketId = if (it.supermarketId == supermarketId) "" else it.supermarketId
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(error = throwable.message) }
                }
        }
    }

    fun save() {
        viewModelScope.launch {
            val current = state.value
            val product = Product(
                id = current.productId,
                name = current.name.toTitleCaseWords(),
                supermarketId = current.supermarketId,
                sectionId = current.sectionId,
                checked = current.checked,
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

    fun delete() {
        viewModelScope.launch {
            val id = state.value.productId
            if (id.isBlank()) return@launch
            runCatching { deleteProductUseCase(id) }
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
    val sectionId: String = "",
    val checked: Boolean = false,
    val price: String = "",
    val quantity: String = "",
    val sections: List<Section> = emptyList(),
    val supermarkets: List<Supermarket> = emptyList(),
    val saved: Boolean = false,
    val error: String? = null
)

private fun String.toTitleCaseWords(): String =
    trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
