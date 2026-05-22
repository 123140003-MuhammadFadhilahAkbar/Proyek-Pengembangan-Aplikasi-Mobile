package com.learncore.data.repository

import com.learncore.data.remote.api.GeminiService
import com.learncore.domain.repository.AIRepository

class AIRepositoryImpl(private val geminiService: GeminiService) : AIRepository {
    override suspend fun generateResponse(
        prompt: String,
        systemPrompt: String?
    ): Result<String> = geminiService.generateContent(prompt, systemPrompt)
}
