package com.ism.qmobilityproduct.remote

import com.ism.qmobilityproduct.data.dto.ProductDto
import com.ism.qmobilityproduct.data.dto.ProductsResponseDto
import com.ism.qmobilityproduct.data.remote.ProductApi

class FakeProductApi : ProductApi {
    var productsResult: ProductsResponseDto? = null
    var productByIdResult: ProductDto? = null
    var error: Exception? = null

    override suspend fun getProducts(limit: Int, skip: Int): ProductsResponseDto {
        error?.let { throw it }
        return productsResult!!
    }

    override suspend fun searchProducts(query: String): ProductsResponseDto {
        error?.let { throw it }
        return productsResult!!
    }

    override suspend fun getProductById(id: Int): ProductDto {
        error?.let { throw it }
        return productByIdResult!!
    }
}