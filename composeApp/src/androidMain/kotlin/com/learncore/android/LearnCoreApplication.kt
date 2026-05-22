package com.learncore.android

import android.app.Application
import com.learncore.core.di.androidModule
import com.learncore.core.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class LearnCoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            platformModules = listOf(androidModule)
        ) {
            androidLogger()
            androidContext(this@LearnCoreApplication)
        }
    }
}
