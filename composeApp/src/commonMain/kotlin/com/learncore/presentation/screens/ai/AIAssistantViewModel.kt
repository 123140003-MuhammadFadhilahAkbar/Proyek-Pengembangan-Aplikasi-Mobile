package com.learncore.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.domain.usecase.AnalyzeProductivityUseCase
import com.learncore.domain.repository.AIRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

data class AIAssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

val QUICK_PROMPTS = listOf(
    "Analisa produktivitasku",
    "Strategi belajar efektif",
    "Cara mengatasi prokrastinasi",
    "Tips manajemen waktu"
)

private val LEARNCORE_SYSTEM_PROMPT = """
    Kamu adalah LearnCore AI, asisten produktivitas cerdas yang terintegrasi dalam aplikasi LearnCore.
    
    Spesialisasi:
    - Matriks Eisenhower dan prioritisasi tugas
    - Teknik Pomodoro dan manajemen waktu
    - Strategi belajar efektif untuk pelajar dan mahasiswa
    - Mengatasi prokrastinasi dan meningkatkan fokus
    - Analisis produktivitas personal
    
    Rules:
    - Gunakan Bahasa Indonesia yang profesional namun ramah
    - Berikan respons yang konkret, actionable, dan terstruktur
    - Maksimal 250 kata per respons kecuali diminta lebih panjang
    - Fokus pada solusi praktis yang bisa langsung diterapkan
""".trimIndent()

class AIAssistantViewModel(
    private val analyzeProductivityUseCase: AnalyzeProductivityUseCase,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, error = null)
    }

    fun sendMessage(customText: String? = null) {
        val text = (customText ?: _uiState.value.inputText).trim()
        if (text.isBlank()) return

        val userMessage = ChatMessage(text = text, isUser = true)
        val loadingMessage = ChatMessage(text = "", isUser = false, isLoading = true)

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage + loadingMessage,
            inputText = "",
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            val isProductivityRequest = text.contains("produktivit", ignoreCase = true) ||
                text.contains("analisa", ignoreCase = true) ||
                text.contains("statistik", ignoreCase = true)

            val result = if (isProductivityRequest) {
                analyzeProductivityUseCase(text)
            } else {
                aiRepository.generateResponse(text, LEARNCORE_SYSTEM_PROMPT)
            }

            val aiMessage = result.fold(
                onSuccess = { response ->
                    ChatMessage(text = response, isUser = false)
                },
                onFailure = { e ->
                    ChatMessage(
                        text = "Maaf, terjadi kesalahan: ${e.message ?: "Unknown error"}. Pastikan API key sudah dikonfigurasi.",
                        isUser = false
                    )
                }
            )

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages.dropLast(1) + aiMessage,
                isLoading = false
            )
        }
    }

    fun clearChat() {
        _uiState.value = AIAssistantUiState()
    }
}
