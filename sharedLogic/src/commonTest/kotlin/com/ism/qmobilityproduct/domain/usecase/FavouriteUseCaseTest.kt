package com.ism.qmobilityproduct.domain.usecase

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.fakes.FakeFavouriteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToggleFavouriteUseCaseTest {

    private val repository = FakeFavouriteRepository()
    private val useCase = ToggleFavouriteUseCase(repository)

    @Test
    fun setFavouriteTrue_addsToRepository() = runTest {
        useCase(sampleProduct, true)
        assertTrue(repository.isFavourite(sampleProduct.id))
    }

    @Test
    fun setFavouriteFalse_removesFromRepository() = runTest {
        useCase(sampleProduct, true)
        useCase(sampleProduct, false)
        assertFalse(repository.isFavourite(sampleProduct.id))
    }

    companion object {
        private val sampleProduct = Product(
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
            images = listOf("img1.jpg"),
        )
    }
}
