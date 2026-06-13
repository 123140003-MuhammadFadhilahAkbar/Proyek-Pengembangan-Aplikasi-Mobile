package com.learncore.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(enableLogging: Boolean = false): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                })
            }
            // Gemini 2.5 Flash (thinking model) butuh waktu lebih lama
            install(HttpTimeout) {
                requestTimeoutMillis  = 120_000  // 2 menit — tunggu response penuh
                connectTimeoutMillis  = 15_000   // 15 detik untuk konek
                socketTimeoutMillis   = 120_000  // 2 menit — baca data dari socket
            }
            if (enableLogging) {
                install(Logging) {
                    logger = Logger.SIMPLE
                    level = LogLevel.BODY
                }
            }
        }
    }
}
