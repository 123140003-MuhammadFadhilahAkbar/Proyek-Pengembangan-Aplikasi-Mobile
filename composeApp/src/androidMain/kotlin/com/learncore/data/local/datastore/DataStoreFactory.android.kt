package com.learncore.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "learncore_prefs")

actual class DataStoreFactory(private val context: Context) {
    actual fun create(): DataStore<Preferences> = context.dataStore
}
