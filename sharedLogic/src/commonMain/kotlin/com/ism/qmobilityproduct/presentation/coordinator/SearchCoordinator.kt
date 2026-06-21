package com.ism.qmobilityproduct.presentation.coordinator

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class SearchState(
    val query: String = "",
    val items: List<Product> = emptyList(),
    val isSearching: Boolean = false,
)

data class SearchConfig(
    val minQueryLength: Int = 2,
    val debounceMs: Long = 300L,
)

class SearchCoordinator(
    private val config: SearchConfig,
    private val repository: ProductRepository,
) {

    private val _query = MutableStateFlow("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchState: Flow<SearchState> = _query
        .map { it.trim() }
        .distinctUntilChanged()
        .debounce(config.debounceMs)
        .flatMapLatest { query ->
            if (query.length < config.minQueryLength) {
                flowOf(SearchState())
            } else {
                flow {
                    emit(SearchState(query = query, isSearching = true))
                    when (val result = repository.searchProducts(query)) {
                        is ProductResult.Success -> {
                            emit(SearchState(query = query, items = result.data, isSearching = false))
                        }
                        is ProductResult.Failure -> {
                            emit(SearchState(query = query, isSearching = false))
                        }
                    }
                }
            }
        }

    fun setQuery(query: String) {
        _query.value = query
    }
}
