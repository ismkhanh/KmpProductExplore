package com.ism.qmobilityproduct.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.model.toUserMessage
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import com.ism.qmobilityproduct.domain.usecase.FavouriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductDetailState {
    data object Loading : ProductDetailState()
    data class Success(val product: Product, val isFavourite: Boolean = false) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}

class DetailViewModel(
    private val productRepository: ProductRepository,
    private val favouriteUseCase: FavouriteUseCase,
) : ViewModel() {

    private val _productDetailState = MutableStateFlow<ProductDetailState>(ProductDetailState.Loading)
    val productDetailState: StateFlow<ProductDetailState> = _productDetailState.asStateFlow()

    init {
        viewModelScope.launch {
            favouriteUseCase.events.collect { event ->
                val currentState = _productDetailState.value
                if (currentState is ProductDetailState.Success && currentState.product.id == event.productId) {
                    _productDetailState.value = currentState.copy(isFavourite = event.isFavourite)
                }
            }
        }
    }

    fun getProductDetails(id: Int) {
        viewModelScope.launch {
            _productDetailState.value = ProductDetailState.Loading
            when (val result = productRepository.getProductById(id)) {
                is ProductResult.Success -> {
                    val isFavourite = favouriteUseCase.isFavourite(result.data.id)
                    _productDetailState.value = ProductDetailState.Success(result.data, isFavourite)
                }
                is ProductResult.Failure -> {
                    _productDetailState.value = ProductDetailState.Error(result.error.toUserMessage())
                }
            }
        }
    }

    fun toggleFavourite() {
        val currentState = _productDetailState.value
        if (currentState is ProductDetailState.Success) {
            viewModelScope.launch {
                favouriteUseCase.toggleFavourite(currentState.product)
            }
        }
    }
}
