package com.afquintana.buycheaper.domain.usecase

import com.afquintana.buycheaper.domain.model.Product
import com.afquintana.buycheaper.domain.model.Supermarket
import com.afquintana.buycheaper.domain.repository.ShoppingRepository
import javax.inject.Inject

class ObserveSectionsUseCase @Inject constructor(private val repository: ShoppingRepository) {
    operator fun invoke() = repository.observeSections()
}

class ObserveProductsUseCase @Inject constructor(private val repository: ShoppingRepository) {
    operator fun invoke() = repository.observeProducts()
}

class ObserveSupermarketsUseCase @Inject constructor(private val repository: ShoppingRepository) {
    operator fun invoke() = repository.observeSupermarkets()
}

class AddSectionUseCase @Inject constructor(private val repository: ShoppingRepository) {
    suspend operator fun invoke(title: String) = repository.addSection(title)
}

class DeleteSectionUseCase @Inject constructor(private val repository: ShoppingRepository) {
    suspend operator fun invoke(sectionId: String) = repository.deleteSection(sectionId)
}

class AddProductUseCase @Inject constructor(private val repository: ShoppingRepository) {
    suspend operator fun invoke(product: Product) = repository.addProduct(product)
}

class UpdateProductUseCase @Inject constructor(private val repository: ShoppingRepository) {
    suspend operator fun invoke(product: Product) = repository.updateProduct(product)
}

class DeleteProductUseCase @Inject constructor(private val repository: ShoppingRepository) {
    suspend operator fun invoke(productId: String) = repository.deleteProduct(productId)
}

class GetProductByIdUseCase @Inject constructor(private val repository: ShoppingRepository) {
    suspend operator fun invoke(id: String) = repository.getProductById(id)
}

class AddSupermarketUseCase @Inject constructor(private val repository: ShoppingRepository) {
    suspend operator fun invoke(supermarket: Supermarket) = repository.addSupermarket(supermarket)
}
