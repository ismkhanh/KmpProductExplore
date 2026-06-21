package com.ism.qmobilityproduct.domain.usecase

import com.ism.qmobilityproduct.domain.FavouriteEvent
import com.ism.qmobilityproduct.domain.FavouriteListener
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository

class ToggleFavouriteUseCase(
    private val favouriteRepository: FavouriteRepository,
    private val favouriteListener: FavouriteListener,
) {

    suspend operator fun invoke(product: Product, favourite: Boolean) {
        if (favourite) {
            favouriteRepository.addFavourite(product)
        } else {
            favouriteRepository.deleteFavourite(product.id)
        }
        favouriteListener.notifyChanged(FavouriteEvent(productId = product.id, isFavourite = favourite))
    }
}
