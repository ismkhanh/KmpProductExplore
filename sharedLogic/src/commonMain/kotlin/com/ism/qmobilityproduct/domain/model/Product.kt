package com.ism.qmobilityproduct.domain.model

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val brand: String,
    val thumbnail: String,
    val images: List<String>,
)

data class ProductPage(
    val products: List<Product>,
    val pageInfo: PageInfo,
)

data class PageInfo(
    val total: Int,
    val skip: Int,
    val limit: Int,
) {
    val hasMore: Boolean get() = skip + limit < total
    val nextSkip: Int get() = skip + limit
}