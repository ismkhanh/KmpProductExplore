package com.ism.qmobilityproduct

import android.app.Application
import com.ism.qmobilityproduct.data.AndroidDatabaseDriverFactory
import com.ism.qmobilityproduct.data.local.DatabaseDriverFactory
import com.ism.qmobilityproduct.di.initKoin
import org.koin.dsl.module

class QMobilityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(listOf(module {
            single<DatabaseDriverFactory> { AndroidDatabaseDriverFactory(this@QMobilityApplication) }
        }))
    }
}
