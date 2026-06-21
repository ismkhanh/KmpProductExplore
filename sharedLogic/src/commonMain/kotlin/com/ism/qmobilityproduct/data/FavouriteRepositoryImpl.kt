package com.ism.qmobilityproduct.data

import com.ism.qmobilityproduct.db.AppDatabase
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FavouriteRepositoryImpl(
    private val database: AppDatabase,
    private val dispatcher: CoroutineDispatcher,
) : FavouriteRepository {

    private val queries get() = database.favouriteQueries

    override suspend fun isFavourite(productId: Int): Boolean = withContext(dispatcher) {
        queries.isFavourite(productId.toLong()).executeAsOne() > 0
    }

    override suspend fun toggleFavourite(product: Product): Unit = withContext(dispatcher) {
        if (isFavourite(product.id)) {
            queries.deleteById(product.id.toLong())
        } else {
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
                images = Json.encodeToString(product.images),
            )
        }
    }

    override suspend fun getAllFavourites(): ProductResult<List<Product>> = withContext(dispatcher) {
        try {
            val products = queries.getAll().executeAsList().map { row ->
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
                    images = Json.decodeFromString(row.images),
                )
            }
            ProductResult.Success(products)
        } catch (e: Exception) {
            ProductResult.Failure(
                com.ism.qmobilityproduct.domain.model.DataError.Unknown(
                    e.message ?: "Unknown error"
                )
            )
        }
    }
}
