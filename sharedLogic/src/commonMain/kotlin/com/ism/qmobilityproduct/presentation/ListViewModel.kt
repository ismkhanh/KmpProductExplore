package com.ism.qmobilityproduct.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.toUserMessage
import com.ism.qmobilityproduct.presentation.coordinator.PageState
import com.ism.qmobilityproduct.presentation.coordinator.PaginationCoordinator
import com.ism.qmobilityproduct.presentation.coordinator.SearchCoordinator
import com.ism.qmobilityproduct.presentation.coordinator.SearchState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListViewModel(
    private val paginationCoordinator: PaginationCoordinator,
    private val searchCoordinator: SearchCoordinator,
) : ViewModel() {

    val uiState: StateFlow<ListUiState> = combine(
        paginationCoordinator.state,
        searchCoordinator.searchState,
    ) { pageState, searchState ->
        toUiState(pageState, searchState)
    }
        .onStart { paginationCoordinator.loadProducts() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListUiState.Loading)

    fun loadMore() {
        viewModelScope.launch {
            paginationCoordinator.loadProducts()
        }
    }

    fun search(query: String) {
        searchCoordinator.setQuery(query)
    }

    fun clearSearch() {
        search("")
    }

    private fun toUiState(
        pageState: PageState,
        searchState: SearchState,
    ): ListUiState = when {
        pageState.error != null -> ListUiState.Error(pageState.error.toUserMessage())
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
