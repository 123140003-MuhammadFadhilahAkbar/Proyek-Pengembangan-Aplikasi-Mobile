package com.learncore.core.di

import com.learncore.core.util.DatabaseDriverFactory
import com.learncore.data.local.datastore.DataStoreFactory
import org.koin.dsl.module

val iosModule = module {
    single { DatabaseDriverFactory() }
    single { DataStoreFactory() }
}
