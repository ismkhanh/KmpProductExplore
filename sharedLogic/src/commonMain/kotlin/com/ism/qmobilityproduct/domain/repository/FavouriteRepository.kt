package com.ism.qmobilityproduct.domain.repository

import com.ism.qmobilityproduct.domain.model.Product

interface FavouriteRepository {
    suspend fun isFavourite(productId: Int): Boolean
    suspend fun addFavourite(product: Product)
    suspend fun deleteFavourite(productId: Int)
    suspend fun getAllFavourites(): List<Product>
}
