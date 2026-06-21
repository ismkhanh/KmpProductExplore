package com.ism.qmobilityproduct

import com.ism.qmobilityproduct.data.IosDatabaseDriverFactory
import com.ism.qmobilityproduct.data.local.DatabaseDriverFactory
import com.ism.qmobilityproduct.di.initKoin
import com.ism.qmobilityproduct.presentation.DetailViewModel
import com.ism.qmobilityproduct.presentation.FavouritesViewModel
import com.ism.qmobilityproduct.presentation.ListViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

fun initKoinIos() {
    initKoin(listOf(module {
        single<DatabaseDriverFactory> { IosDatabaseDriverFactory() }
    }))
}

class KoinDependencies : KoinComponent {
    val listViewModel: ListViewModel = get()
    val detailViewModel: DetailViewModel = get()
    val favouritesViewModel: FavouritesViewModel = get()
}
