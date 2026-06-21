package com.ism.qmobilityproduct.data

import com.ism.qmobilityproduct.data.mapper.toDomain
import com.ism.qmobilityproduct.data.remote.ProductApi
import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.model.PageInfo
import com.ism.qmobilityproduct.domain.model.ProductPage
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ProductRepositoryImpl(
    private val productApi: ProductApi,
    private val dispatcher: CoroutineDispatcher,
) : ProductRepository {

    override suspend fun getProducts(limit: Int, skip: Int) = safeApiCall {
        val response = productApi.getProducts(limit, skip)
        ProductPage(
            products = response.products.map { it.toDomain() },
            pageInfo = PageInfo(
                total = response.total,
                skip = response.skip,
                limit = response.limit,
            ),
        )
    }

    override suspend fun searchProducts(query: String) = safeApiCall {
        val response = productApi.searchProducts(query)
        response.products.map { it.toDomain() }
    }

    override suspend fun getProductById(id: Int) = safeApiCall {
        productApi.getProductById(id).toDomain()
    }

    private suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): ProductResult<T> {
        return try {
            withContext(dispatcher) { ProductResult.Success(block()) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            ProductResult.Failure(
                DataError.Server(
                    code = e.response.status.value,
                    message = e.message ?: "Server error",
                )
            )
        } catch (e: IOException) {
            ProductResult.Failure(DataError.Network(e.message ?: "Network error"))
        } catch (e: Exception) {
            ProductResult.Failure(DataError.Unknown(e.message ?: "Unknown error"))
        }
    }
}
