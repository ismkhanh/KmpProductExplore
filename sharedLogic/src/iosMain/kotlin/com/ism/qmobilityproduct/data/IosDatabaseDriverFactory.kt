package com.ism.qmobilityproduct.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.ism.qmobilityproduct.data.local.DatabaseDriverFactory
import com.ism.qmobilityproduct.db.AppDatabase

class IosDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(AppDatabase.Schema, "app.db")
    }
}
