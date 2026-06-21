package com.ism.qmobilityproduct.presentation

import com.ism.qmobilityproduct.domain.listener.FavouriteEvent
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.fakes.FakeFavouriteListener
import com.ism.qmobilityproduct.fakes.FakeFavouriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {

    private lateinit var favouriteRepository: FakeFavouriteRepository
    private lateinit var favouriteListener: FakeFavouriteListener
    private lateinit var viewModel: FavouritesViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        favouriteRepository = FakeFavouriteRepository()
        favouriteListener = FakeFavouriteListener()
        viewModel = FavouritesViewModel(favouriteListener, favouriteRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoading() {
        assertIs<FavouritesUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun emitsEmptyWhenNoFavourites() = runTest {
        val values = mutableListOf<FavouritesUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { values.add(it) }
        }
        advanceUntilIdle()

        assertIs<FavouritesUiState.Empty>(viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun loadsExistingFavourites() = runTest {
        favouriteRepository.addFavourite(sampleProduct1)
        favouriteRepository.addFavourite(sampleProduct2)

        val values = mutableListOf<FavouritesUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { values.add(it) }
        }
        advanceUntilIdle()

        val state = assertIs<FavouritesUiState.Success>(viewModel.uiState.value)
        assertEquals(2, state.products.size)
        job.cancel()
    }

    @Test
    fun refreshesWhenListenerEmitsEvent() = runTest {
        val values = mutableListOf<FavouritesUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { values.add(it) }
        }
        advanceUntilIdle()

        assertIs<FavouritesUiState.Empty>(viewModel.uiState.value)

        favouriteRepository.addFavourite(sampleProduct1)
        favouriteListener.notifyChanged(FavouriteEvent(sampleProduct1.id, true))
        advanceUntilIdle()

        val updatedState = assertIs<FavouritesUiState.Success>(viewModel.uiState.value)
        assertEquals(1, updatedState.products.size)
        assertEquals(sampleProduct1.id, updatedState.products.first().id)
        job.cancel()
    }

    @Test
    fun becomesEmptyWhenLastFavouriteRemoved() = runTest {
        favouriteRepository.addFavourite(sampleProduct1)

        val values = mutableListOf<FavouritesUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { values.add(it) }
        }
        advanceUntilIdle()

        assertIs<FavouritesUiState.Success>(viewModel.uiState.value)

        favouriteRepository.deleteFavourite(sampleProduct1.id)
        favouriteListener.notifyChanged(FavouriteEvent(sampleProduct1.id, false))
        advanceUntilIdle()

        assertIs<FavouritesUiState.Empty>(viewModel.uiState.value)
        job.cancel()
    }

    companion object {
        private val sampleProduct1 = Product(
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
        private val sampleProduct2 = Product(
            id = 2,
            title = "Tablet",
            description = "A tablet",
            category = "electronics",
            price = 499.0,
            discountPercentage = 10.0,
            rating = 4.0,
            stock = 5,
            brand = "Acme",
            thumbnail = "thumb2.jpg",
            images = listOf("img2.jpg"),
        )
    }
}
