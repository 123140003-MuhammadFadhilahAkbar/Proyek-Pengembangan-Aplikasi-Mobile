package com.learncore.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.core.network.NetworkMonitor
import com.learncore.domain.usecase.AnalyzeProductivityUseCase
import com.learncore.domain.repository.AIRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

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
    Kamu adalah LearnCore AI, asisten produktivitas dalam aplikasi LearnCore.
    
    Aturan WAJIB:
    - Jawab SINGKAT dan PADAT — maksimal 5 poin atau 100 kata
    - Langsung ke inti, tanpa basa-basi atau pembukaan panjang
    - Tidak perlu ucapan seperti "Tentu!", "Baik!", "Halo!" — langsung jawab
    - Gunakan poin pendek, bukan paragraf panjang
    - Bahasa Indonesia, profesional tapi ringkas
""".trimIndent()

class AIAssistantViewModel(
    private val analyzeProductivityUseCase: AnalyzeProductivityUseCase,
    private val aiRepository: AIRepository
) : ViewModel(), KoinComponent {

    private val networkMonitor: NetworkMonitor by inject()

    private val _uiState = MutableStateFlow(AIAssistantUiState())
    val uiState: StateFlow<AIAssistantUiState> = _uiState.asStateFlow()

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, error = null)
    }

    fun sendMessage(customText: String? = null) {
        val text = (customText ?: _uiState.value.inputText).trim()
        if (text.isBlank() || _uiState.value.isLoading) return

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
            val isOnline = networkMonitor.isOnline.first()
            if (!isOnline) {
                val offlineMsg = ChatMessage(
                    id = nextMessageId(),
                    text = "📡 Tidak ada koneksi internet.",
                    isUser = false
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages.map { if (it.id == loadingId) offlineMsg else it },
                    isLoading = false
                )
                return@launch
            }

            val isProductivityRequest = text.contains("produktivit", ignoreCase = true) ||
                    text.contains("analisa", ignoreCase = true) ||
                    text.contains("analisis", ignoreCase = true) ||
                    text.contains("statistik", ignoreCase = true) ||
                    text.contains("tugas", ignoreCase = true) ||
                    text.contains("prioritas", ignoreCase = true)

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
                        val msg = e.message ?: ""
                        when {
                            msg.contains("503") || msg.contains("high demand") || msg.contains("overloaded") ->
                                "⏳ Server sibuk, coba lagi sebentar."
                            msg.contains("429") || msg.contains("quota") || msg.contains("rate limit") ->
                                "⏱️ Batas permintaan tercapai, tunggu sebentar."
                            msg.contains("500") ->
                                "🔧 Server gangguan, coba lagi nanti."
                            msg.contains("401") || msg.contains("API key") ->
                                "🔑 API key tidak valid. Periksa GEMINI_API_KEY di local.properties."
                            msg.contains("timeout") || msg.contains("Timeout") || msg.contains("SocketTimeout") ||
                                    msg.contains("ConnectException") || msg.contains("UnresolvedAddressException") ->
                                "📡 Koneksi timeout. Pastikan internet stabil lalu coba lagi."
                            msg.contains("Empty response") ->
                                "🤔 AI tidak merespons. Coba ulangi."
                            else ->
                                "❌ Gagal: $msg"
                        }
                    }
                ),
                isUser = false
            )

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
