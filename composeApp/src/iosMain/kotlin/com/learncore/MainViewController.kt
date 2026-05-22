package com.learncore

import androidx.compose.ui.window.ComposeUIViewController
import com.learncore.core.di.initKoin
import com.learncore.core.di.iosModule

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin(platformModules = listOf(iosModule)) }
) { App() }
