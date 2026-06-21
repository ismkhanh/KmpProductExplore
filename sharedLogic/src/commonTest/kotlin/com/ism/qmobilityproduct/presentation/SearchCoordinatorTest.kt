package com.ism.qmobilityproduct.presentation

import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.fakes.FakeProductRepository
import com.ism.qmobilityproduct.presentation.coordinator.SearchConfig
import com.ism.qmobilityproduct.presentation.coordinator.SearchCoordinator
import com.ism.qmobilityproduct.presentation.coordinator.SearchState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchCoordinatorTest {

    private lateinit var repository: FakeProductRepository
    private lateinit var coordinator: SearchCoordinator

    @BeforeTest
    fun setUp() {
        repository = FakeProductRepository()
        coordinator = SearchCoordinator(SearchConfig(debounceMs = 0), repository)
    }

    @Test
    fun blankQueryEmitsEmptyState() = runTest {
        val emissions = mutableListOf<SearchState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.searchState.toList(emissions)
        }

        coordinator.setQuery("")

        val last = emissions.last()
        assertEquals("", last.query)
        assertTrue(last.items.isEmpty())
        assertFalse(last.isSearching)

        job.cancel()
    }

    @Test
    fun successfulSearchEmitsLoadingThenResults() = runTest {
        repository.searchProductsResult = ProductResult.Success(
            listOf(product(1), product(2))
        )

        val emissions = mutableListOf<SearchState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.searchState.toList(emissions)
        }

        coordinator.setQuery("phone")

        val loading = emissions.first { it.query == "phone" && it.isSearching }
        assertTrue(loading.items.isEmpty())

        val result = emissions.last()
        assertEquals("phone", result.query)
        assertEquals(2, result.items.size)
        assertFalse(result.isSearching)

        job.cancel()
    }

    @Test
    fun failureEmitsLoadingThenEmptyItems() = runTest {
        repository.searchProductsResult = ProductResult.Failure(
            DataError.Network("offline")
        )

        val emissions = mutableListOf<SearchState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.searchState.toList(emissions)
        }

        coordinator.setQuery("phone")

        val result = emissions.last()
        assertEquals("phone", result.query)
        assertTrue(result.items.isEmpty())
        assertFalse(result.isSearching)

        job.cancel()
    }

    @Test
    fun queryIsTrimmedBeforeSearch() = runTest {
        repository.searchProductsResult = ProductResult.Success(listOf(product(1)))

        val emissions = mutableListOf<SearchState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.searchState.toList(emissions)
        }

        coordinator.setQuery("  phone  ")

        assertEquals("phone", repository.lastSearchQuery)

        job.cancel()
    }

    @Test
    fun singleCharQueryDoesNotTriggerSearch() = runTest {
        val emissions = mutableListOf<SearchState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.searchState.toList(emissions)
        }

        coordinator.setQuery("a")

        val last = emissions.last()
        assertTrue(last.items.isEmpty())
        assertFalse(last.isSearching)
        assertNull(repository.lastSearchQuery)

        job.cancel()
    }

    @Test
    fun whitespaceOnlyQueryEmitsEmptyState() = runTest {
        val emissions = mutableListOf<SearchState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            coordinator.searchState.toList(emissions)
        }

        coordinator.setQuery("   ")

        val last = emissions.last()
        assertEquals("", last.query)
        assertTrue(last.items.isEmpty())
        assertFalse(last.isSearching)
        assertNull(repository.lastSearchQuery)

        job.cancel()
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
