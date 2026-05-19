package com.learncore.core.network

import com.learncore.android.BuildConfig

actual object ApiConfig {
    actual val geminiApiKey: String
        get() = BuildConfig.GEMINI_API_KEY
}
