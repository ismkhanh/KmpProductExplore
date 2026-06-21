package com.ism.qmobilityproduct.presentation.coordinator

import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.PageInfo
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PageState(
    val items: List<Product> = emptyList(),
    val pageInfo: PageInfo? = null,
    val isLoading: Boolean = false,
    val error: DataError? = null,
) {
    val hasMore: Boolean get() = pageInfo?.hasMore ?: true
}

data class PageConfig(
    val pageSize: Int = 10,
)

class PaginationCoordinator(
    private val config: PageConfig,
    private val repository: ProductRepository,
) {

    private val loading = atomic(false)
    private val _state = MutableStateFlow(PageState())
    val state: StateFlow<PageState> = _state.asStateFlow()

    suspend fun loadProducts() {
        if (!loading.compareAndSet(expect = false, update = true)) return
        try {
            val current = _state.value
            if (!current.hasMore) return
            _state.update { it.copy(isLoading = true, error = null) }

            val skip = current.pageInfo?.nextSkip ?: 0

            when (val result = repository.getProducts(config.pageSize, skip)) {
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
                        error = result.error,
                    )
                }
            }
        } finally {
            loading.value = false
        }
    }
}
