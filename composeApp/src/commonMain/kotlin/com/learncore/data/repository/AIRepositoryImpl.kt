package com.learncore.data.repository

import com.learncore.data.remote.api.GeminiService
import com.learncore.domain.repository.AIRepository
import kotlinx.coroutines.delay

class AIRepositoryImpl(private val geminiService: GeminiService) : AIRepository {

    override suspend fun generateResponse(
        prompt: String,
        systemPrompt: String?
    ): Result<String> {
        // Coba pertama kali
        val firstAttempt = geminiService.generateContent(prompt, systemPrompt)

        // Jika 503/overload, tunggu 2 detik lalu coba sekali lagi
        if (firstAttempt.isFailure) {
            val errMsg = firstAttempt.exceptionOrNull()?.message ?: ""
            val isRetryable = errMsg.contains("503") ||
                    errMsg.contains("high demand") ||
                    errMsg.contains("overloaded") ||
                    errMsg.contains("500")

            if (isRetryable) {
                delay(2000)
                return geminiService.generateContent(prompt, systemPrompt)
            }
        }

        return firstAttempt
    }
}
