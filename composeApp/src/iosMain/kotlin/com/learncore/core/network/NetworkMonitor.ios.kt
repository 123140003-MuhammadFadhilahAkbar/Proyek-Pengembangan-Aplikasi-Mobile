package com.learncore.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkMonitorImpl : NetworkMonitor {
    override val isOnline: Flow<Boolean> = MutableStateFlow(true)
}