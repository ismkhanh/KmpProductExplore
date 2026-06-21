package com.ism.qmobilityproduct.presentation

import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.PageInfo
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductPage
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.fakes.FakeProductRepository
import com.ism.qmobilityproduct.presentation.coordinator.PageConfig
import com.ism.qmobilityproduct.presentation.coordinator.PaginationCoordinator
import com.ism.qmobilityproduct.presentation.coordinator.SearchConfig
import com.ism.qmobilityproduct.presentation.coordinator.SearchCoordinator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class ListViewModelTest {

    private lateinit var repository: FakeProductRepository
    private lateinit var paginationCoordinator: PaginationCoordinator
    private lateinit var searchCoordinator: SearchCoordinator
    private lateinit var viewModel: ListViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        repository = FakeProductRepository()
        paginationCoordinator = PaginationCoordinator(PageConfig(), repository)
        searchCoordinator = SearchCoordinator(config = SearchConfig(debounceMs = 0), repository = repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoading() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(products = emptyList(), pageInfo = PageInfo(total = 0, skip = 0, limit = 10))
        )
        viewModel = ListViewModel(paginationCoordinator, searchCoordinator)

        val state = viewModel.uiState.value
        assertIs<ListUiState.Loading>(state)
    }

    @Test
    fun afterInitialLoad_emitsProductsState() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1), product(2)),
                pageInfo = PageInfo(total = 20, skip = 0, limit = 10),
            )
        )
        viewModel = ListViewModel(paginationCoordinator, searchCoordinator)

        val state = viewModel.uiState.first { it is ListUiState.Products }

        val products = assertIs<ListUiState.Products>(state)
        assertEquals(2, products.items.size)
        assertFalse(products.isLoadingMore)
    }

    @Test
    fun loadMore_appendsProducts() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1)),
                pageInfo = PageInfo(total = 20, skip = 0, limit = 10),
            )
        )
        viewModel = ListViewModel(paginationCoordinator, searchCoordinator)
        viewModel.uiState.first { it is ListUiState.Products }

        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(2)),
                pageInfo = PageInfo(total = 20, skip = 10, limit = 10),
            )
        )
        viewModel.loadMore()
        advanceUntilIdle()

        val state = assertIs<ListUiState.Products>(viewModel.uiState.value)
        assertEquals(2, state.items.size)
        assertEquals(listOf(1, 2), state.items.map { it.id })
    }

    @Test
    fun error_emitsErrorState() = runTest {
        repository.getProductsResult = ProductResult.Failure(
            DataError.Network("no internet")
        )
        viewModel = ListViewModel(paginationCoordinator, searchCoordinator)

        val state = viewModel.uiState.first { it is ListUiState.Error }

        val error = assertIs<ListUiState.Error>(state)
        assertEquals("No internet connection. Please check your network.", error.message)
    }

    @Test
    fun search_emitsSearchState() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1)),
                pageInfo = PageInfo(total = 10, skip = 0, limit = 10),
            )
        )
        repository.searchProductsResult = ProductResult.Success(
            listOf(product(5), product(6))
        )
        viewModel = ListViewModel(paginationCoordinator, searchCoordinator)
        viewModel.uiState.first { it is ListUiState.Products }

        viewModel.search("phone")
        advanceUntilIdle()

        val state = viewModel.uiState.first { it is ListUiState.Search }
        val search = assertIs<ListUiState.Search>(state)
        assertEquals("phone", search.query)
        assertEquals(2, search.items.size)
        assertFalse(search.isSearching)
    }

    @Test
    fun clearSearch_returnsToProductsState() = runTest {
        repository.getProductsResult = ProductResult.Success(
            ProductPage(
                products = listOf(product(1)),
                pageInfo = PageInfo(total = 10, skip = 0, limit = 10),
            )
        )
        repository.searchProductsResult = ProductResult.Success(
            listOf(product(5))
        )
        viewModel = ListViewModel(paginationCoordinator, searchCoordinator)
        viewModel.uiState.first { it is ListUiState.Products }

        viewModel.search("phone")
        viewModel.uiState.first { it is ListUiState.Search }

        viewModel.clearSearch()
        advanceUntilIdle()

        val state = viewModel.uiState.first { it is ListUiState.Products }
        assertIs<ListUiState.Products>(state)
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
