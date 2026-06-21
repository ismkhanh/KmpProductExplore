package com.ism.qmobilityproduct.presentation

import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.PageInfo
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductPage
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.fakes.FakeProductRepository
import com.ism.qmobilityproduct.presentation.coordinator.PageConfig
import com.ism.qmobilityproduct.presentation.coordinator.PaginationCoordinator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PaginationCoordinatorTest {

    private lateinit var repository: FakeProductRepository
    private lateinit var coordinator: PaginationCoordinator

    @BeforeTest
    fun setUp() {
        repository = FakeProductRepository()
        coordinator = PaginationCoordinator(PageConfig(), repository)
    }

    @Test
    fun initialStateIsEmpty() {
        val state = coordinator.state.value
        assertTrue(state.items.isEmpty())
        assertNull(state.pageInfo)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.hasMore)
    }

    @Test
    fun loadProducts_successPopulatesItemsAndPageInfo() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1), product(2)),
                pageInfo = PageInfo(total = 20, skip = 0, limit = 10),
            )
        )

        coordinator.loadProducts()

        val state = coordinator.state.value
        assertEquals(2, state.items.size)
        assertEquals(1, state.items[0].id)
        assertEquals(2, state.items[1].id)
        assertEquals(20, state.pageInfo?.total)
        assertEquals(0, state.pageInfo?.skip)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.hasMore)
    }

    @Test
    fun loadProducts_networkErrorSetsErrorMessage() = runTest {
        repository.getProductsResult = ProductResult.Failure(
            DataError.Network("timeout")
        )

        coordinator.loadProducts()

        val state = coordinator.state.value
        assertTrue(state.items.isEmpty())
        assertFalse(state.isLoading)
        assertIs<DataError.Network>(state.error)
    }

    @Test
    fun loadProducts_serverErrorSetsError() = runTest {
        repository.getProductsResult = ProductResult.Failure(
            DataError.Server(code = 500, message = "Internal Server Error")
        )

        coordinator.loadProducts()

        val state = coordinator.state.value
        val error = assertIs<DataError.Server>(state.error)
        assertEquals(500, error.code)
    }

    @Test
    fun loadProducts_unknownErrorSetsError() = runTest {
        repository.getProductsResult = ProductResult.Failure(
            DataError.Unknown("something broke")
        )

        coordinator.loadProducts()

        val state = coordinator.state.value
        assertIs<DataError.Unknown>(state.error)
    }

    @Test
    fun loadProducts_secondPageAppendsItems() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1), product(2)),
                pageInfo = PageInfo(total = 20, skip = 0, limit = 10),
            )
        )
        coordinator.loadProducts()

        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(3), product(4)),
                pageInfo = PageInfo(total = 20, skip = 10, limit = 10),
            )
        )
        coordinator.loadProducts()

        val state = coordinator.state.value
        assertEquals(4, state.items.size)
        assertEquals(listOf(1, 2, 3, 4), state.items.map { it.id })
        assertFalse(state.hasMore)
    }

    @Test
    fun loadProducts_doesNotLoadWhenNoMorePages() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1)),
                pageInfo = PageInfo(total = 1, skip = 0, limit = 10),
            )
        )
        coordinator.loadProducts()
        assertFalse(coordinator.state.value.hasMore)

        repository.getProductsCallCount = 0
        coordinator.loadProducts()
        assertEquals(0, repository.getProductsCallCount)
    }

    @Test
    fun loadProducts_passesCorrectSkipOnSecondPage() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1)),
                pageInfo = PageInfo(total = 20, skip = 0, limit = 10),
            )
        )
        coordinator.loadProducts()

        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(2)),
                pageInfo = PageInfo(total = 20, skip = 10, limit = 10),
            )
        )
        coordinator.loadProducts()

        assertEquals(10, repository.lastRequestedSkip)
    }

    @Test
    fun loadProducts_concurrentCallsOnlyExecuteOnce() = runTest {
        repository.getProductsDelayMs = 100
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1)),
                pageInfo = PageInfo(total = 20, skip = 0, limit = 10),
            )
        )

        launch { coordinator.loadProducts() }
        launch { coordinator.loadProducts() }
        launch { coordinator.loadProducts() }
        advanceUntilIdle()

        assertEquals(1, repository.getProductsCallCount)
        assertEquals(1, coordinator.state.value.items.size)
    }
}

private fun product(id: Int) = Product(
    id = id,
    title = "Product $id",
    description = "Description $id",
    category = "category",
    price = 10.0,
    discountPercentage = 0.0,
    rating = 4.0,
    stock = 5,
    brand = "Brand",
    thumbnail = "thumb.jpg",
    images = emptyList(),
)
