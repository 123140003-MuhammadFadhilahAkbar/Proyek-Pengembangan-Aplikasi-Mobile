package com.learncore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.learncore.data.local.datastore.UserPreferences
import com.learncore.presentation.navigation.AppNavHost
import com.learncore.presentation.theme.LearnCoreTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val userPreferences: UserPreferences = koinInject()
    val isDarkMode by userPreferences.isDarkMode.collectAsState(initial = false)

    LearnCoreTheme(darkTheme = isDarkMode) {
        AppNavHost()
    }
}
