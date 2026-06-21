package com.ism.qmobilityproduct.fakes

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository

class FakeFavouriteRepository : FavouriteRepository {

    private val favourites = mutableMapOf<Int, Product>()
    var getAllFavouritesResult: ProductResult<List<Product>>? = null

    override suspend fun isFavourite(productId: Int): Boolean {
        return favourites.containsKey(productId)
    }

    override suspend fun toggleFavourite(product: Product) {
        if (favourites.containsKey(product.id)) {
            favourites.remove(product.id)
        } else {
            favourites[product.id] = product
        }
    }

    override suspend fun getAllFavourites(): ProductResult<List<Product>> {
        getAllFavouritesResult?.let { return it }
        return ProductResult.Success(favourites.values.toList())
    }
}
