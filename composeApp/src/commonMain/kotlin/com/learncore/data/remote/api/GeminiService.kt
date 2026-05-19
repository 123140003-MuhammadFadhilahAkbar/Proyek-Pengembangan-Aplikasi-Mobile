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
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GeminiService(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private const val MODEL = "gemini-2.5-flash"
    }

    suspend fun generateContent(
        prompt: String,
        systemPrompt: String? = null
    ): Result<String> = runCatching {
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
                maxOutputTokens = 1024
            )
        )

        val response: GeminiResponse = client.post("$BASE_URL/models/$MODEL:generateContent") {
            contentType(ContentType.Application.Json)
            parameter("key", ApiConfig.geminiApiKey)
            setBody(request)
        }.body()

        response.candidates?.firstOrNull()
            ?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Empty response from AI")
    }
}
