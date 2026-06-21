package com.ism.qmobilityproduct.domain.repository

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult

interface FavouriteRepository {
    suspend fun isFavourite(productId: Int): Boolean
    suspend fun toggleFavourite(product: Product)
    suspend fun getAllFavourites(): ProductResult<List<Product>>
}