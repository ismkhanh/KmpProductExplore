package com.ism.qmobilityproduct.data.remote

import com.ism.qmobilityproduct.data.dto.ProductDto
import com.ism.qmobilityproduct.data.dto.ProductsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

interface ProductApi {
    suspend fun getProducts(limit: Int, skip: Int): ProductsResponseDto
    suspend fun searchProducts(query: String): ProductsResponseDto
    suspend fun getProductById(id: Int): ProductDto
}

class KtorProductApi(
    private val baseUrl: String,
    private val client: HttpClient,
) : ProductApi {

    override suspend fun getProducts(limit: Int, skip: Int): ProductsResponseDto =
        client.get("$baseUrl/products") {
            parameter("limit", limit)
            parameter("skip", skip)
        }.body()

    override suspend fun searchProducts(query: String): ProductsResponseDto =
        client.get("$baseUrl/products/search") {
            parameter("q", query)
        }.body()

    override suspend fun getProductById(id: Int): ProductDto =
        client.get("$baseUrl/products/$id").body()
}