package com.ism.qmobilityproduct.domain.usecase

import com.ism.qmobilityproduct.domain.model.PageInfo
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.model.toUserMessage
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PageState(
    val items: List<Product> = emptyList(),
    val pageInfo: PageInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val hasMore: Boolean get() = pageInfo?.hasMore ?: true
}

class PaginatedProductsUseCase(private val repository: ProductRepository) {

    private val _state = MutableStateFlow(PageState())
    val state: StateFlow<PageState> = _state.asStateFlow()

    suspend fun loadProducts() {
        val current = _state.value
        if (current.isLoading || !current.hasMore) return
        _state.update { it.copy(isLoading = true, error = null) }

        val skip = current.pageInfo?.nextSkip ?: 0

        when (val result = repository.getProducts(PAGE_SIZE, skip)) {
            is ProductResult.Success -> _state.update {
                it.copy(
                    items = current.items + result.data.products,
                    pageInfo = result.data.pageInfo,
                    isLoading = false,
                )
            }
            is ProductResult.Failure -> _state.update {
                it.copy(
                    isLoading = false,
                    error = result.error.toUserMessage(),
                )
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}
