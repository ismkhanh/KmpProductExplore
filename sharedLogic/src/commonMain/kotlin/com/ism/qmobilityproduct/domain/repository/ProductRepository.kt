package com.ism.qmobilityproduct.domain.repository

import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductPage

interface ProductRepository {
    suspend fun getProducts(limit: Int, skip: Int): ProductResult<ProductPage>
    suspend fun searchProducts(query: String): ProductResult<List<Product>>
    suspend fun getProductById(id: Int): ProductResult<Product>
}