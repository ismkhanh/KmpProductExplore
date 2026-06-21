package com.ism.qmobilityproduct.domain.repository

import com.ism.qmobilityproduct.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface FavouriteRepository {
    fun observeAll(): Flow<List<Product>>
    suspend fun isFavourite(productId: Int): Boolean
    suspend fun addFavourite(product: Product)
    suspend fun deleteFavourite(productId: Int)
}
