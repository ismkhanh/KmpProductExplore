package com.ism.qmobilityproduct.viewmodels

import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.usecase.FavouriteUseCase
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
    private lateinit var favouriteUseCase: FavouriteUseCase
    private lateinit var viewModel: FavouritesViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        favouriteRepository = FakeFavouriteRepository()
        favouriteUseCase = FavouriteUseCase(favouriteRepository)
        viewModel = FavouritesViewModel(favouriteUseCase, favouriteRepository)
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
    fun loadsEmptyListWhenNoFavourites() = runTest {
        val values = mutableListOf<FavouritesUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { values.add(it) }
        }
        advanceUntilIdle()

        val state = assertIs<FavouritesUiState.Success>(viewModel.uiState.value)
        assertEquals(0, state.products.size)
        job.cancel()
    }

    @Test
    fun loadsExistingFavourites() = runTest {
        favouriteRepository.toggleFavourite(sampleProduct1)
        favouriteRepository.toggleFavourite(sampleProduct2)

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
    fun emitsErrorStateOnFailure() = runTest {
        favouriteRepository.getAllFavouritesResult = ProductResult.Failure(
            DataError.Unknown("db error")
        )

        val values = mutableListOf<FavouritesUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { values.add(it) }
        }
        advanceUntilIdle()

        val state = assertIs<FavouritesUiState.Error>(viewModel.uiState.value)
        assertEquals("Something went wrong. Please try again.", state.message)
        job.cancel()
    }

    @Test
    fun refreshesWhenFavouriteEventEmitted() = runTest {
        val values = mutableListOf<FavouritesUiState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { values.add(it) }
        }
        advanceUntilIdle()

        val emptyState = assertIs<FavouritesUiState.Success>(viewModel.uiState.value)
        assertEquals(0, emptyState.products.size)

        favouriteUseCase.toggleFavourite(sampleProduct1)
        advanceUntilIdle()

        val updatedState = assertIs<FavouritesUiState.Success>(viewModel.uiState.value)
        assertEquals(1, updatedState.products.size)
        assertEquals(sampleProduct1.id, updatedState.products.first().id)
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
