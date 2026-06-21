package com.ism.qmobilityproduct.fakes

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository

class FakeFavouriteRepository : FavouriteRepository {

    private val favourites = mutableMapOf<Int, Product>()

    override suspend fun isFavourite(productId: Int): Boolean {
        return favourites.containsKey(productId)
    }

    override suspend fun addFavourite(product: Product) {
        favourites[product.id] = product
    }

    override suspend fun deleteFavourite(productId: Int) {
        favourites.remove(productId)
    }

    override suspend fun getAllFavourites(): List<Product> {
        return favourites.values.toList()
    }
}
