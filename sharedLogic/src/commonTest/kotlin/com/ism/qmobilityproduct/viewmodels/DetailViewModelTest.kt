package com.ism.qmobilityproduct.viewmodels

import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
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
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private lateinit var repository: FakeProductRepository
    private lateinit var viewModel: DetailViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeProductRepository()
        viewModel = DetailViewModel(repository)
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
