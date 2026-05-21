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
    val id: Long,
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

// Thread-safe monotonically increasing ID generator — avoids duplicate IDs
// when two messages are created within the same millisecond
private var _messageCounter = 0L
private fun nextMessageId(): Long = ++_messageCounter

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
        if (text.isBlank() || _uiState.value.isLoading) return

        // Use monotonic counter instead of currentTimeMillis to guarantee unique IDs
        val userMessage = ChatMessage(id = nextMessageId(), text = text, isUser = true)
        val loadingMessage = ChatMessage(id = nextMessageId(), text = "", isUser = false, isLoading = true)
        val loadingId = loadingMessage.id

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

            val result = runCatching {
                if (isProductivityRequest) {
                    analyzeProductivityUseCase(text).getOrThrow()
                } else {
                    aiRepository.generateResponse(text, LEARNCORE_SYSTEM_PROMPT).getOrThrow()
                }
            }

            val aiMessage = ChatMessage(
                id = nextMessageId(),
                text = result.fold(
                    onSuccess = { it },
                    onFailure = { e ->
                        when {
                            e.message?.contains("401") == true ||
                            e.message?.contains("API key") == true ->
                                "API key Gemini tidak valid atau belum dikonfigurasi. Tambahkan GEMINI_API_KEY di local.properties."
                            e.message?.contains("Empty response") == true ->
                                "AI tidak memberikan respons. Coba ulangi pertanyaanmu."
                            e.message?.contains("UnresolvedAddressException") == true ||
                            e.message?.contains("ConnectException") == true ->
                                "Tidak ada koneksi internet. Periksa koneksimu dan coba lagi."
                            else ->
                                "Terjadi kesalahan: ${e.message ?: "Unknown error"}. Coba lagi."
                        }
                    }
                ),
                isUser = false
            )

            // Replace the loading message identified by its unique ID
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages.map { msg ->
                    if (msg.id == loadingId) aiMessage else msg
                },
                isLoading = false
            )
        }
    }

    fun clearChat() {
        _uiState.value = AIAssistantUiState()
    }
}
