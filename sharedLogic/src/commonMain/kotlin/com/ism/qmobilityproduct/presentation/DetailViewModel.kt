package com.ism.qmobilityproduct.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.model.toUserMessage
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import com.ism.qmobilityproduct.domain.usecase.ToggleFavouriteUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val productRepository: ProductRepository,
    private val favouriteRepository: FavouriteRepository,
    private val toggleFavouriteUseCase: ToggleFavouriteUseCase,
) : ViewModel() {

    private val _productDetailState = MutableStateFlow<ProductDetailState>(ProductDetailState.Loading)
    val productDetailState: StateFlow<ProductDetailState> = _productDetailState.asStateFlow()

    fun getProductDetails(id: Int) {
        viewModelScope.launch {
            _productDetailState.value = ProductDetailState.Loading
            val productDetailDeferred = async { productRepository.getProductById(id) }
            val isFavouriteDeferred = async { favouriteRepository.isFavourite(id) }
            when (val result = productDetailDeferred.await()) {
                is ProductResult.Success -> {
                    val isFavourite = isFavouriteDeferred.await()
                    _productDetailState.value = ProductDetailState.Success(result.data, isFavourite)
                }
                is ProductResult.Failure -> {
                    isFavouriteDeferred.cancel()
                    _productDetailState.value = ProductDetailState.Error(result.error.toUserMessage())
                }
            }
        }
    }

    fun toggleFavourite() {
        val currentState = _productDetailState.value
        if (currentState is ProductDetailState.Success) {
            val newFavourite = !currentState.isFavourite
            if (_productDetailState.compareAndSet(currentState, currentState.copy(isFavourite = newFavourite))) {
                viewModelScope.launch {
                    toggleFavouriteUseCase(currentState.product, newFavourite)
                }
            }
        }
    }
}

sealed class ProductDetailState {
    data object Loading : ProductDetailState()
    data class Success(val product: Product, val isFavourite: Boolean = false) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}
