package com.learncore.core.di

import com.learncore.core.network.NetworkMonitor
import com.learncore.core.network.NetworkMonitorImpl
import com.learncore.core.notification.DeadlineScheduler
import com.learncore.core.notification.DeadlineSchedulerImpl
import com.learncore.core.notification.PomodoroNotifier
import com.learncore.core.notification.PomodoroNotifierImpl
import com.learncore.core.util.DatabaseDriverFactory
import com.learncore.data.local.datastore.DataStoreFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { DataStoreFactory(androidContext()) }
    single { NetworkMonitorImpl(androidContext()) } bind NetworkMonitor::class
    single { PomodoroNotifierImpl(androidContext()) } bind PomodoroNotifier::class
    single { DeadlineSchedulerImpl(androidContext()) } bind DeadlineScheduler::class
}
