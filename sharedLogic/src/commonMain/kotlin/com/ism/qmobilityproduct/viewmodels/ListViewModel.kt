package com.ism.qmobilityproduct.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.usecase.PageState
import com.ism.qmobilityproduct.domain.usecase.PaginatedProductsUseCase
import com.ism.qmobilityproduct.domain.usecase.SearchProductsUseCase
import com.ism.qmobilityproduct.domain.usecase.SearchState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ListUiState {
    data object Loading : ListUiState
    data class Products(
        val items: List<Product>,
        val isLoadingMore: Boolean,
    ) : ListUiState
    data class Search(
        val query: String,
        val items: List<Product>,
        val isSearching: Boolean,
    ) : ListUiState
    data class Error(val message: String) : ListUiState
}

class ListViewModel(
    private val paginatedProductsUseCase: PaginatedProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
) : ViewModel() {

    val uiState: StateFlow<ListUiState> = combine(
        paginatedProductsUseCase.state,
        searchProductsUseCase.searchState,
    ) { pageState, searchState ->
        toUiState(pageState, searchState)
    }
        .onStart { paginatedProductsUseCase.loadProducts() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState.Loading)

    fun loadMore() {
        viewModelScope.launch {
            paginatedProductsUseCase.loadProducts()
        }
    }

    fun search(query: String) {
        searchProductsUseCase.setQuery(query)
    }

    fun clearSearch() {
        search("")
    }

    private fun toUiState(
        pageState: PageState,
        searchState: SearchState,
    ): ListUiState = when {
        pageState.error != null -> ListUiState.Error(pageState.error)
        pageState.pageInfo == null && pageState.isLoading -> ListUiState.Loading
        searchState.query.isNotBlank() -> ListUiState.Search(
            query = searchState.query,
            items = searchState.items,
            isSearching = searchState.isSearching,
        )
        else -> ListUiState.Products(
            items = pageState.items,
            isLoadingMore = pageState.isLoading,
        )
    }
}