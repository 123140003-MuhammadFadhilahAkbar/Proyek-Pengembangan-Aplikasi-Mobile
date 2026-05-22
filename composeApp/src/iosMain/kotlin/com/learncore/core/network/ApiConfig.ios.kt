package com.learncore.core.network

actual object ApiConfig {
    // Isi dengan Gemini API key untuk build iOS.
    // Untuk keamanan, bisa baca dari Info.plist atau environment variable saat build.
    actual val geminiApiKey: String = ""
}
