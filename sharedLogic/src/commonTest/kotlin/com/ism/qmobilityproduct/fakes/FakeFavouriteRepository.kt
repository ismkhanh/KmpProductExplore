package com.ism.qmobilityproduct.fakes

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeFavouriteRepository : FavouriteRepository {

    private val favourites = MutableStateFlow<List<Product>>(emptyList())

    override fun observeAll(): Flow<List<Product>> = favourites

    override suspend fun isFavourite(productId: Int): Boolean {
        return favourites.value.any { it.id == productId }
    }

    override suspend fun addFavourite(product: Product) {
        favourites.update { current -> current + product }
    }

    override suspend fun deleteFavourite(productId: Int) {
        favourites.update { current -> current.filter { it.id != productId } }
    }
}
