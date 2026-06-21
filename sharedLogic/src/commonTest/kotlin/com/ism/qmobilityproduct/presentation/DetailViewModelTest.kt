package com.ism.qmobilityproduct.presentation

import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.domain.usecase.ToggleFavouriteUseCase
import com.ism.qmobilityproduct.fakes.FakeFavouriteListener
import com.ism.qmobilityproduct.fakes.FakeFavouriteRepository
import com.ism.qmobilityproduct.fakes.FakeProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private lateinit var repository: FakeProductRepository
    private lateinit var favouriteRepository: FakeFavouriteRepository
    private lateinit var favouriteListener: FakeFavouriteListener
    private lateinit var toggleFavouriteUseCase: ToggleFavouriteUseCase
    private lateinit var viewModel: DetailViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeProductRepository()
        favouriteRepository = FakeFavouriteRepository()
        favouriteListener = FakeFavouriteListener()
        toggleFavouriteUseCase = ToggleFavouriteUseCase(favouriteRepository, favouriteListener)
        viewModel = DetailViewModel(repository, favouriteRepository, toggleFavouriteUseCase)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoading() {
        assertIs<ProductDetailState.Loading>(viewModel.productDetailState.value)
    }

    @Test
    fun getProductDetails_successEmitsProduct() = runTest {
        repository.getProductByIdResult = ProductResult.Success(sampleProduct)

        viewModel.getProductDetails(1)
        advanceUntilIdle()

        val state = assertIs<ProductDetailState.Success>(viewModel.productDetailState.value)
        assertEquals(1, state.product.id)
        assertEquals("Phone", state.product.title)
    }

    @Test
    fun getProductDetails_passesCorrectId() = runTest {
        repository.getProductByIdResult = ProductResult.Success(sampleProduct)

        viewModel.getProductDetails(42)
        advanceUntilIdle()

        assertEquals(42, repository.lastRequestedProductId)
    }

    @Test
    fun getProductDetails_networkErrorEmitsErrorState() = runTest {
        repository.getProductByIdResult = ProductResult.Failure(
            DataError.Network("timeout")
        )

        viewModel.getProductDetails(1)
        advanceUntilIdle()

        val state = assertIs<ProductDetailState.Error>(viewModel.productDetailState.value)
        assertEquals("No internet connection. Please check your network.", state.message)
    }

    @Test
    fun getProductDetails_serverErrorEmitsErrorState() = runTest {
        repository.getProductByIdResult = ProductResult.Failure(
            DataError.Server(code = 404, message = "Not Found")
        )

        viewModel.getProductDetails(1)
        advanceUntilIdle()

        val state = assertIs<ProductDetailState.Error>(viewModel.productDetailState.value)
        assertEquals("Server error (404). Please try again later.", state.message)
    }

    @Test
    fun getProductDetails_unknownErrorEmitsErrorState() = runTest {
        repository.getProductByIdResult = ProductResult.Failure(
            DataError.Unknown("unexpected")
        )

        viewModel.getProductDetails(1)
        advanceUntilIdle()

        val state = assertIs<ProductDetailState.Error>(viewModel.productDetailState.value)
        assertEquals("Something went wrong. Please try again.", state.message)
    }

    @Test
    fun getProductDetails_isFavouriteFalseByDefault() = runTest {
        repository.getProductByIdResult = ProductResult.Success(sampleProduct)

        viewModel.getProductDetails(1)
        advanceUntilIdle()

        val state = assertIs<ProductDetailState.Success>(viewModel.productDetailState.value)
        assertFalse(state.isFavourite)
    }

    @Test
    fun toggleFavourite_updatesStateImmediately() = runTest {
        repository.getProductByIdResult = ProductResult.Success(sampleProduct)
        viewModel.getProductDetails(1)
        advanceUntilIdle()

        viewModel.toggleFavourite()

        val state = assertIs<ProductDetailState.Success>(viewModel.productDetailState.value)
        assertTrue(state.isFavourite)
    }

    @Test
    fun toggleFavouriteTwice_removesFromFavourites() = runTest {
        repository.getProductByIdResult = ProductResult.Success(sampleProduct)
        viewModel.getProductDetails(1)
        advanceUntilIdle()

        viewModel.toggleFavourite()
        viewModel.toggleFavourite()

        val state = assertIs<ProductDetailState.Success>(viewModel.productDetailState.value)
        assertFalse(state.isFavourite)
    }

    @Test
    fun toggleFavourite_notifiesListener() = runTest {
        repository.getProductByIdResult = ProductResult.Success(sampleProduct)
        viewModel.getProductDetails(1)
        advanceUntilIdle()

        viewModel.toggleFavourite()
        advanceUntilIdle()

        assertEquals(1, favouriteListener.emittedEvents.size)
        assertTrue(favouriteListener.emittedEvents[0].isFavourite)
        assertEquals(sampleProduct.id, favouriteListener.emittedEvents[0].productId)
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
