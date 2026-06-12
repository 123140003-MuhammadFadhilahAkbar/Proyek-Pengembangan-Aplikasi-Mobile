package com.learncore.data.remote.api

import com.learncore.core.network.ApiConfig
import com.learncore.data.remote.dto.GeminiContent
import com.learncore.data.remote.dto.GeminiPart
import com.learncore.data.remote.dto.GeminiRequest
import com.learncore.data.remote.dto.GeminiResponse
import com.learncore.data.remote.dto.GenerationConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class GeminiService(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private const val MODEL = "gemini-2.5-flash"
    }

    suspend fun generateContent(
        prompt: String,
        systemPrompt: String? = null
    ): Result<String> = runCatching {
        val apiKey = ApiConfig.geminiApiKey
        if (apiKey.isBlank()) {
            throw Exception("API key Gemini belum dikonfigurasi. Tambahkan GEMINI_API_KEY di file local.properties.")
        }

        val contents = mutableListOf<GeminiContent>()

        if (systemPrompt != null) {
            contents.add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = systemPrompt)),
                    role = "user"
                )
            )
            contents.add(
                GeminiContent(
                    parts = listOf(GeminiPart(text = "Baik, saya siap membantu.")),
                    role = "model"
                )
            )
        }

        contents.add(
            GeminiContent(
                parts = listOf(GeminiPart(text = prompt)),
                role = "user"
            )
        )

        val request = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = 0.7,
                maxOutputTokens = 8192
            )
        )

        val httpResponse = client.post("$BASE_URL/models/$MODEL:generateContent") {
            contentType(ContentType.Application.Json)
            parameter("key", apiKey)
            setBody(request)
        }

        // Tangkap HTTP error sebelum parse JSON
        if (!httpResponse.status.isSuccess()) {
            val statusCode = httpResponse.status.value
            val rawBody = runCatching { httpResponse.bodyAsText() }.getOrDefault("")
            throw Exception("Gemini API error ($statusCode): $rawBody")
        }

        val response: GeminiResponse = httpResponse.body()

        // Cek error dari body JSON
        if (response.error != null) {
            val code = response.error.code ?: 0
            val msg = response.error.message ?: "Unknown API error"
            throw Exception("Gemini API error ($code): $msg")
        }

        val candidate = response.candidates?.firstOrNull()

        // Jika finish reason MAX_TOKENS, tetap kembalikan teks yang ada (tidak throw)
        val finishReason = candidate?.finishReason
        val text = candidate?.content?.parts?.firstOrNull()?.text

        if (!text.isNullOrBlank()) {
            // Jika terpotong karena token, tambahkan keterangan
            if (finishReason == "MAX_TOKENS") {
                return@runCatching "$text\n\n_(Jawaban terpotong — coba tanyakan lebih spesifik)_"
            }
            return@runCatching text
        }

        // Jika benar-benar kosong
        throw Exception("Empty response dari AI (finishReason: $finishReason)")
    }
}
