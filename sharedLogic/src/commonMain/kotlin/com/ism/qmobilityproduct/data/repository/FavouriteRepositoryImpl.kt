package com.ism.qmobilityproduct.data.repository

import com.ism.qmobilityproduct.db.AppDatabase
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class FavouriteRepositoryImpl(
    private val database: AppDatabase,
    private val dispatcher: CoroutineDispatcher,
) : FavouriteRepository {

    private val queries get() = database.favouriteQueries

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

    override suspend fun getAllFavourites(): List<Product> = withContext(dispatcher) {
        queries.getAll().executeAsList().map { row ->
            Product(
                id = row.id.toInt(),
                title = row.title,
                description = row.description,
                category = row.category,
                price = row.price,
                discountPercentage = row.discountPercentage,
                rating = row.rating,
                stock = row.stock.toInt(),
                brand = row.brand,
                thumbnail = row.thumbnail,
                images = Json.Default.decodeFromString(row.images),
            )
        }
    }
}