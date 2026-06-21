package com.ism.qmobilityproduct.di

import com.ism.qmobilityproduct.data.DatabaseDriverFactory
import com.ism.qmobilityproduct.data.FavouriteListenerImpl
import com.ism.qmobilityproduct.data.FavouriteRepositoryImpl
import com.ism.qmobilityproduct.data.ProductRepositoryImpl
import com.ism.qmobilityproduct.data.remote.KtorProductApi
import com.ism.qmobilityproduct.data.remote.ProductApi
import com.ism.qmobilityproduct.db.AppDatabase
import com.ism.qmobilityproduct.domain.FavouriteListener
import com.ism.qmobilityproduct.domain.repository.FavouriteRepository
import com.ism.qmobilityproduct.domain.repository.ProductRepository
import com.ism.qmobilityproduct.domain.usecase.ToggleFavouriteUseCase
import com.ism.qmobilityproduct.domain.usecase.PaginatedProductsUseCase
import com.ism.qmobilityproduct.domain.usecase.SearchConfig
import com.ism.qmobilityproduct.domain.usecase.SearchProductsUseCase
import com.ism.qmobilityproduct.viewmodels.DetailViewModel
import com.ism.qmobilityproduct.viewmodels.FavouritesViewModel
import com.ism.qmobilityproduct.viewmodels.ListViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
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
    single { AppDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single<FavouriteRepository> { FavouriteRepositoryImpl(get(), Dispatchers.IO) }
    single<FavouriteListener> { FavouriteListenerImpl() }
}

val domainModule = module {
    factory { PaginatedProductsUseCase(get()) }
    factory { SearchProductsUseCase(config = SearchConfig(), repository = get()) }
    single { ToggleFavouriteUseCase(get(), get()) }
}

internal val viewModelModule = module {
    factory { ListViewModel(paginatedProductsUseCase = get(), searchProductsUseCase = get()) }
    factory { DetailViewModel(productRepository = get(), favouriteRepository = get(), toggleFavouriteUseCase = get()) }
    factory { FavouritesViewModel(favouriteListener = get(), favouriteRepository = get()) }
}

fun initKoin(platformModules: List<Module> = emptyList()) {
    startKoin {
        modules(platformModules + listOf(
            networkModule,
            dataModule,
            domainModule,
            viewModelModule,
        ))
    }
}