package com.ism.qmobilityproduct.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.ism.qmobilityproduct.db.AppDatabase
import com.ism.qmobilityproduct.db.FavouriteProduct
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class FavouriteRepositoryImpl(
    private val database: AppDatabase,
    private val dispatcher: CoroutineDispatcher,
) : FavouriteRepository {

    private val queries get() = database.favouriteQueries

    override fun observeAll(): Flow<List<Product>> {
        return queries.getAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toProduct() } }
    }

    override suspend fun isFavourite(productId: Int): Boolean = withContext(dispatcher) {
        queries.isFavourite(productId.toLong()).executeAsOne() > 0
    }

    override suspend fun addFavourite(product: Product): Unit = withContext(dispatcher) {
        queries.insertOrReplace(
            id = product.id.toLong(),
            title = product.title,
            description = product.description,
            category = product.category,
            price = product.price,
            discountPercentage = product.discountPercentage,
            rating = product.rating,
            stock = product.stock.toLong(),
            brand = product.brand,
            thumbnail = product.thumbnail,
            images = Json.Default.encodeToString(product.images),
        )
    }

    override suspend fun deleteFavourite(productId: Int): Unit = withContext(dispatcher) {
        queries.deleteById(productId.toLong())
    }

    private fun FavouriteProduct.toProduct(): Product = Product(
        id = id.toInt(),
        title = title,
        description = description,
        category = category,
        price = price,
        discountPercentage = discountPercentage,
        rating = rating,
        stock = stock.toInt(),
        brand = brand,
        thumbnail = thumbnail,
        images = Json.Default.decodeFromString(images),
    )
}
