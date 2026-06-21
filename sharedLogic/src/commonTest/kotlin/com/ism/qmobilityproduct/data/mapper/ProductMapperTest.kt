package com.ism.qmobilityproduct.data.mapper

import com.ism.qmobilityproduct.data.dto.ProductDto
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductMapperTest {

    @Test
    fun productDto_toDomain_mapsAllFields() {
        val dto = ProductDto(
            id = 1,
            title = "Phone",
            description = "A phone",
            category = "electronics",
            price = 999.0,
            discountPercentage = 5.0,
            rating = 4.5,
            stock = 10,
            brand = "Acme",
            thumbnail = "thumb.jpg",
            images = listOf("img1.jpg", "img2.jpg"),
        )

        val product = dto.toDomain()

        assertEquals(dto.id, product.id)
        assertEquals(dto.title, product.title)
        assertEquals(dto.description, product.description)
        assertEquals(dto.category, product.category)
        assertEquals(dto.price, product.price)
        assertEquals(dto.discountPercentage, product.discountPercentage)
        assertEquals(dto.rating, product.rating)
        assertEquals(dto.stock, product.stock)
        assertEquals(dto.brand, product.brand)
        assertEquals(dto.thumbnail, product.thumbnail)
        assertEquals(dto.images, product.images)
    }
}