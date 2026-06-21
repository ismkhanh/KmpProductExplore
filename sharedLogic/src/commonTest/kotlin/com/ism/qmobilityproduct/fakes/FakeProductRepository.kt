package com.ism.qmobilityproduct.fakes

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductPage
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import kotlinx.coroutines.delay

class FakeProductRepository : ProductRepository {
    var getProductsResult: ProductResult<ProductPage>? = null
    var getProductsCallCount = 0
    var lastRequestedSkip = 0
    var getProductsDelayMs: Long = 0

    override suspend fun getProducts(limit: Int, skip: Int): ProductResult<ProductPage> {
        getProductsCallCount++
        lastRequestedSkip = skip
        if (getProductsDelayMs > 0) delay(getProductsDelayMs)
        return getProductsResult!!
    }

    var searchProductsResult: ProductResult<List<Product>>? = null
    var lastSearchQuery: String? = null

    override suspend fun searchProducts(query: String): ProductResult<List<Product>> {
        lastSearchQuery = query
        return searchProductsResult!!
    }

    var getProductByIdResult: ProductResult<Product>? = null
    var lastRequestedProductId: Int? = null

    override suspend fun getProductById(id: Int): ProductResult<Product> {
        lastRequestedProductId = id
        return getProductByIdResult!!
    }
}