package com.ism.qmobilityproduct.domain.usecase

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class FavouriteEvent(
    val productId: Int,
    val isFavourite: Boolean,
)

class FavouriteUseCase(
    private val favouriteRepository: FavouriteRepository,
) {

    private val _events = MutableSharedFlow<FavouriteEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<FavouriteEvent> = _events.asSharedFlow()

    suspend fun isFavourite(productId: Int): Boolean {
        return favouriteRepository.isFavourite(productId)
    }

    suspend fun toggleFavourite(product: Product) {
        favouriteRepository.toggleFavourite(product)
        val isFavourite = favouriteRepository.isFavourite(product.id)
        _events.emit(FavouriteEvent(productId = product.id, isFavourite = isFavourite))
    }

}