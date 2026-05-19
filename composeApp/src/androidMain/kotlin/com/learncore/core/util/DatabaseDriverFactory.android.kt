package com.learncore.core.util

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.learncore.data.local.LearnCoreDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = LearnCoreDatabase.Schema,
            context = context,
            name = "learncore.db"
        )
    }
}
