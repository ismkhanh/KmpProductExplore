package com.ism.qmobilityproduct.di

import com.ism.qmobilityproduct.data.ProductRepositoryImpl
import com.ism.qmobilityproduct.data.remote.KtorProductApi
import com.ism.qmobilityproduct.data.remote.ProductApi
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import com.ism.qmobilityproduct.domain.usecase.PaginatedProductsUseCase
import com.ism.qmobilityproduct.domain.usecase.SearchConfig
import com.ism.qmobilityproduct.domain.usecase.SearchProductsUseCase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val BASE_URL_QUALIFIER = "baseUrl"

val networkModule = module {
    single(named(BASE_URL_QUALIFIER)) { "https://dummyjson.com" }

    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }
    single {
        HttpClient {
            expectSuccess = true
            install(ContentNegotiation) {
                json(get())
            }
            install(Logging) {
                level = LogLevel.BODY
            }
            install(HttpTimeout)
        }
    }
}

val dataModule = module {
    single<ProductApi> { KtorProductApi(baseUrl = get(named(BASE_URL_QUALIFIER)), client = get()) }
    single<ProductRepository> { ProductRepositoryImpl(productApi = get(), dispatcher = Dispatchers.IO) }
}

val domainModule = module {
    factory { PaginatedProductsUseCase(get()) }
    factory { SearchProductsUseCase(config = SearchConfig(), repository = get()) }
}