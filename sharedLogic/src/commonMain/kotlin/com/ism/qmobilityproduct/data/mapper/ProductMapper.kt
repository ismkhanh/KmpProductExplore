package com.ism.qmobilityproduct.data.mapper

import com.ism.qmobilityproduct.data.dto.ProductDto
import com.ism.qmobilityproduct.domain.model.Product

fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    price = price,
    discountPercentage = discountPercentage,
    rating = rating,
    stock = stock,
    brand = brand,
    thumbnail = thumbnail,
    images = images,
)