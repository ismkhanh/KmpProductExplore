package com.ism.qmobilityproduct.domain.usecase

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository

class ToggleFavouriteUseCase(
    private val favouriteRepository: FavouriteRepository,
) {

    suspend operator fun invoke(product: Product, favourite: Boolean) {
        if (favourite) {
            favouriteRepository.addFavourite(product)
        } else {
            favouriteRepository.deleteFavourite(product.id)
        }
    }
}
