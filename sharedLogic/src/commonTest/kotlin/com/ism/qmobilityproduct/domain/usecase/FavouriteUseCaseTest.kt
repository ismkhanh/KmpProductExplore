package com.ism.qmobilityproduct.domain.usecase

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.fakes.FakeFavouriteListener
import com.ism.qmobilityproduct.fakes.FakeFavouriteRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToggleFavouriteUseCaseTest {

    private val repository = FakeFavouriteRepository()
    private val listener = FakeFavouriteListener()
    private val useCase = ToggleFavouriteUseCase(repository, listener)

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

    @Test
    fun setFavourite_notifiesListenerWithCorrectEvent() = runTest {
        useCase(sampleProduct, true)

        assertEquals(1, listener.emittedEvents.size)
        assertEquals(sampleProduct.id, listener.emittedEvents[0].productId)
        assertTrue(listener.emittedEvents[0].isFavourite)
    }

    @Test
    fun setFavouriteFalse_notifiesListenerWithFalse() = runTest {
        useCase(sampleProduct, true)
        useCase(sampleProduct, false)

        assertEquals(2, listener.emittedEvents.size)
        assertTrue(listener.emittedEvents[0].isFavourite)
        assertFalse(listener.emittedEvents[1].isFavourite)
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
