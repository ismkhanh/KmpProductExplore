package com.ism.qmobilityproduct.data

import com.ism.qmobilityproduct.data.dto.ProductDto
import com.ism.qmobilityproduct.data.dto.ProductsResponseDto
import com.ism.qmobilityproduct.data.repository.ProductRepositoryImpl
import com.ism.qmobilityproduct.domain.model.DataError
import com.ism.qmobilityproduct.domain.model.Product
import com.ism.qmobilityproduct.domain.model.ProductPage
import com.ism.qmobilityproduct.domain.model.ProductResult
import com.ism.qmobilityproduct.fakes.FakeProductApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ProductRepositoryImplTest {

    private val sampleDto = ProductDto(
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

    private val sampleResponse = ProductsResponseDto(
        products = listOf(sampleDto),
        total = 50,
        skip = 0,
        limit = 10,
    )

    private lateinit var api: FakeProductApi
    private lateinit var repo: ProductRepositoryImpl

    @BeforeTest
    fun setUp() {
        api = FakeProductApi()
    }

    @Test
    fun getProducts_returnsSuccessWithMappedProducts() = runTest {
        api.productsResult = sampleResponse
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.getProducts(limit = 10, skip = 0)

        val page = assertIs<ProductResult.Success<ProductPage>>(result).data
        assertEquals(1, page.products.size)
        assertEquals("Phone", page.products.first().title)
        assertEquals(50, page.pageInfo.total)
        assertEquals(0, page.pageInfo.skip)
        assertEquals(10, page.pageInfo.limit)
    }

    @Test
    fun getProducts_returnsNetworkErrorOnIOException() = runTest {
        api.error = IOException("timeout")
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.getProducts(limit = 10, skip = 0)

        val failure = assertIs<ProductResult.Failure>(result)
        val error = assertIs<DataError.Network>(failure.error)
        assertEquals("timeout", error.message)
    }

    @Test
    fun getProducts_returnsUnknownErrorOnGenericException() = runTest {
        api.error = RuntimeException("parse failed")
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.getProducts(limit = 10, skip = 0)

        val failure = assertIs<ProductResult.Failure>(result)
        val error = assertIs<DataError.Unknown>(failure.error)
        assertEquals("parse failed", error.message)
    }

    @Test
    fun getProducts_rethrowsCancellationException() = runTest {
        api.error = CancellationException("cancelled")
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        assertFailsWith<CancellationException> {
            repo.getProducts(limit = 10, skip = 0)
        }
    }

    @Test
    fun searchProducts_returnsSuccessWithMappedProducts() = runTest {
        api.productsResult = sampleResponse
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.searchProducts("phone")

        val products = assertIs<ProductResult.Success<List<Product>>>(result).data
        assertEquals(1, products.size)
    }

    @Test
    fun searchProducts_returnsNetworkErrorOnIOException() = runTest {
        api.error = IOException("no connection")
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.searchProducts("phone")

        val failure = assertIs<ProductResult.Failure>(result)
        assertIs<DataError.Network>(failure.error)
    }

    @Test
    fun searchProducts_returnsUnknownErrorOnGenericException() = runTest {
        api.error = RuntimeException("boom")
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.searchProducts("phone")

        val failure = assertIs<ProductResult.Failure>(result)
        assertIs<DataError.Unknown>(failure.error)
    }

    @Test
    fun getProductById_returnsSuccessWithMappedProduct() = runTest {
        api.productByIdResult = sampleDto
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.getProductById(1)

        val product = assertIs<ProductResult.Success<Product>>(result).data
        assertEquals(1, product.id)
        assertEquals("Phone", product.title)
    }

    @Test
    fun getProductById_returnsNetworkErrorOnIOException() = runTest {
        api.error = IOException("dns failure")
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.getProductById(1)

        val failure = assertIs<ProductResult.Failure>(result)
        val error = assertIs<DataError.Network>(failure.error)
        assertEquals("dns failure", error.message)
    }

    @Test
    fun getProductById_returnsUnknownErrorOnGenericException() = runTest {
        api.error = IllegalStateException("unexpected")
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        val result = repo.getProductById(1)

        val failure = assertIs<ProductResult.Failure>(result)
        assertIs<DataError.Unknown>(failure.error)
    }

    @Test
    fun getProductById_rethrowsCancellationException() = runTest {
        api.error = CancellationException("job cancelled")
        repo = ProductRepositoryImpl(api, StandardTestDispatcher(testScheduler))

        assertFailsWith<CancellationException> {
            repo.getProductById(1)
        }
    }
}
