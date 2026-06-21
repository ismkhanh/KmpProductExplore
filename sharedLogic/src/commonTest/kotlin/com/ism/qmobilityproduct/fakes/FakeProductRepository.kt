package com.ism.qmobilityproduct.fakes

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductPage
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.repository.ProductRepository

class FakeProductRepository : ProductRepository {
    var getProductsResult: ProductResult<ProductPage>? = null
    var getProductsCallCount = 0
    var lastRequestedSkip = 0

    override suspend fun getProducts(limit: Int, skip: Int): ProductResult<ProductPage> {
        getProductsCallCount++
        lastRequestedSkip = skip
        return getProductsResult!!
    }

    override suspend fun searchProducts(query: String): ProductResult<List<Product>> {
        throw NotImplementedError()
    }

    override suspend fun getProductById(id: Int): ProductResult<Product> {
        throw NotImplementedError()
    }
}