package com.ism.qmobilityproduct

import android.app.Application
import com.ism.qmobilityproduct.di.initKoin

class QMobilityApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}