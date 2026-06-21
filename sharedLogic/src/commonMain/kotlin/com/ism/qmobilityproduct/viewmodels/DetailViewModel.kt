package com.ism.qmobilityproduct.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.model.toUserMessage
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductDetailState {
    data object Loading : ProductDetailState()
    data class Success(val product: Product) : ProductDetailState()
    data class Error(val message: String) : ProductDetailState()
}

class DetailViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _productDetailState = MutableStateFlow<ProductDetailState>(ProductDetailState.Loading)
    val productDetailState: StateFlow<ProductDetailState> = _productDetailState.asStateFlow()

    fun getProductDetails(id: Int) {
        viewModelScope.launch {
            _productDetailState.value = ProductDetailState.Loading
            _productDetailState.value = when (val result = productRepository.getProductById(id)) {
                is ProductResult.Success -> ProductDetailState.Success(result.data)
                is ProductResult.Failure -> ProductDetailState.Error(result.error.toUserMessage())
            }
        }
    }
}
