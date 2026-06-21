package com.ism.qmobilityproduct.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ism.qmobilityproduct.domain.FavouriteListener
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

sealed interface FavouritesUiState {
    data object Loading : FavouritesUiState
    data class Success(val products: List<Product>) : FavouritesUiState
    data object Empty : FavouritesUiState
}

class FavouritesViewModel(
    favouriteListener: FavouriteListener,
    private val favouriteRepository: FavouriteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavouritesUiState>(FavouritesUiState.Loading)
    val uiState: StateFlow<FavouritesUiState> = _uiState
        .onStart { loadFavourites() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavouritesUiState.Loading)

    init {
        favouriteListener.events.onEach {
            loadFavourites()
        }.launchIn(viewModelScope)
    }

    private suspend fun loadFavourites() {
        val products = favouriteRepository.getAllFavourites()
        _uiState.value = if (products.isEmpty()) {
            FavouritesUiState.Empty
        } else {
            FavouritesUiState.Success(products)
        }
    }
}
