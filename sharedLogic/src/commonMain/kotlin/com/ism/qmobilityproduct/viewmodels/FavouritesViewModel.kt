package com.ism.qmobilityproduct.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.model.toUserMessage
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import com.ism.qmobilityproduct.domain.usecase.FavouriteUseCase
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
    data class Error(val message: String) : FavouritesUiState
}

class FavouritesViewModel(
    private val favouriteUseCase: FavouriteUseCase,
    private val favouriteRepository: FavouriteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavouritesUiState>(FavouritesUiState.Loading)
    val uiState: StateFlow<FavouritesUiState> = _uiState
        .onStart { loadFavourites() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavouritesUiState.Loading)

    init {
        favouriteUseCase.events.onEach {
            loadFavourites()
        }.launchIn(viewModelScope)
    }

    private suspend fun loadFavourites() {
        when (val result = favouriteRepository.getAllFavourites()) {
            is ProductResult.Success -> _uiState.value = FavouritesUiState.Success(result.data)
            is ProductResult.Failure -> _uiState.value = FavouritesUiState.Error(result.error.toUserMessage())
        }
    }
}
