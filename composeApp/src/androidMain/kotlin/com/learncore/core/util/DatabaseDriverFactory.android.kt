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
            name = "learncore.db",
            callback = object : AndroidSqliteDriver.Callback(LearnCoreDatabase.Schema) {
                override fun onUpgrade(
                    db: androidx.sqlite.db.SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    // Migrasi dari v1 (schema lama: tanpa category/description/deadline)
                    // ke v2 (schema baru: dengan ketiga kolom tersebut).
                    // runCatching per-statement untuk safety jika kolom sudah ada.
                    if (oldVersion < 2) {
                        runCatching {
                            db.execSQL("ALTER TABLE TaskEntity ADD COLUMN category TEXT NOT NULL DEFAULT ''")
                        }
                        runCatching {
                            db.execSQL("ALTER TABLE TaskEntity ADD COLUMN description TEXT NOT NULL DEFAULT ''")
                        }
                        runCatching {
                            db.execSQL("ALTER TABLE TaskEntity ADD COLUMN deadline INTEGER")
                        }
                    }
                    if (oldVersion < 3) {
                        runCatching {
                            db.execSQL("ALTER TABLE TaskEntity ADD COLUMN reminder_minutes INTEGER")
                        }
                    }
                }
            }
        )
    }
}
