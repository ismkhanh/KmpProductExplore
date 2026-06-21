package com.ism.qmobilityproduct.domain.usecase

import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.fakes.FakeFavouriteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FavouriteUseCaseTest {

    private val repository = FakeFavouriteRepository()
    private val useCase = FavouriteUseCase(repository)

    @Test
    fun isFavourite_returnsFalseWhenNotFavourited() = runTest {
        assertFalse(useCase.isFavourite(sampleProduct.id))
    }

    @Test
    fun isFavourite_returnsTrueAfterToggle() = runTest {
        useCase.toggleFavourite(sampleProduct)
        assertTrue(useCase.isFavourite(sampleProduct.id))
    }

    @Test
    fun toggleFavourite_addsToFavourites() = runTest {
        useCase.toggleFavourite(sampleProduct)
        assertTrue(repository.isFavourite(sampleProduct.id))
    }

    @Test
    fun toggleFavouriteTwice_removesFromFavourites() = runTest {
        useCase.toggleFavourite(sampleProduct)
        useCase.toggleFavourite(sampleProduct)
        assertFalse(repository.isFavourite(sampleProduct.id))
    }

    @Test
    fun toggleFavourite_emitsEventWithIsFavouriteTrue() = runTest {
        var event: FavouriteEvent? = null
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            event = useCase.events.first()
        }

        useCase.toggleFavourite(sampleProduct)
        job.join()

        assertEquals(sampleProduct.id, event?.productId)
        assertTrue(event!!.isFavourite)
    }

    @Test
    fun toggleFavouriteTwice_emitsEventWithIsFavouriteFalse() = runTest {
        val events = mutableListOf<FavouriteEvent>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase.events.collect { events.add(it) }
        }

        useCase.toggleFavourite(sampleProduct)
        useCase.toggleFavourite(sampleProduct)
        job.cancel()

        assertEquals(2, events.size)
        assertTrue(events[0].isFavourite)
        assertFalse(events[1].isFavourite)
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
