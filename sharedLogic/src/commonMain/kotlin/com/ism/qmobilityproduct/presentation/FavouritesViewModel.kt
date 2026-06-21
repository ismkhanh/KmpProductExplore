package com.ism.qmobilityproduct.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class FavouritesViewModel(
    favouriteRepository: FavouriteRepository,
) : ViewModel() {

    val uiState: StateFlow<FavouritesUiState> = favouriteRepository.observeAll()
        .map { products ->
            if (products.isEmpty()) FavouritesUiState.Empty
            else FavouritesUiState.Success(products)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavouritesUiState.Loading)
}

sealed interface FavouritesUiState {
    data object Loading : FavouritesUiState
    data class Success(val products: List<Product>) : FavouritesUiState
    data object Empty : FavouritesUiState
}
