package com.learncore.presentation

import com.learncore.presentation.screens.ai.AIAssistantUiState
import com.learncore.presentation.screens.ai.ChatMessage
import com.learncore.presentation.screens.ai.QUICK_PROMPTS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AIAssistantUiStateTest {

    // ===== ChatMessage =====

    @Test
    fun `ChatMessage isUser true for user message`() {
        val msg = ChatMessage(id = 1L, text = "Halo", isUser = true)
        assertTrue(msg.isUser)
    }

    @Test
    fun `ChatMessage isUser false for AI message`() {
        val msg = ChatMessage(id = 2L, text = "Halo juga", isUser = false)
        assertFalse(msg.isUser)
    }

    @Test
    fun `ChatMessage isLoading default is false`() {
        val msg = ChatMessage(id = 1L, text = "text", isUser = false)
        assertFalse(msg.isLoading)
    }

    @Test
    fun `ChatMessage isLoading true for loading state`() {
        val msg = ChatMessage(id = 1L, text = "", isUser = false, isLoading = true)
        assertTrue(msg.isLoading)
    }

    @Test
    fun `ChatMessage stores text correctly`() {
        val msg = ChatMessage(id = 1L, text = "Analisa produktivitasku", isUser = true)
        assertEquals("Analisa produktivitasku", msg.text)
    }

    @Test
    fun `ChatMessage stores id correctly`() {
        val msg = ChatMessage(id = 42L, text = "Test", isUser = true)
        assertEquals(42L, msg.id)
    }

    // ===== AIAssistantUiState =====

    @Test
    fun `default messages is empty`() {
        assertTrue(AIAssistantUiState().messages.isEmpty())
    }

    @Test
    fun `default inputText is empty`() {
        assertEquals("", AIAssistantUiState().inputText)
    }

    @Test
    fun `default isLoading is false`() {
        assertFalse(AIAssistantUiState().isLoading)
    }

    @Test
    fun `default error is null`() {
        assertNull(AIAssistantUiState().error)
    }

    @Test
    fun `copy with messages reflects new messages`() {
        val msg = ChatMessage(id = 1L, text = "Hi", isUser = true)
        val state = AIAssistantUiState().copy(messages = listOf(msg))
        assertEquals(1, state.messages.size)
        assertEquals("Hi", state.messages.first().text)
    }

    @Test
    fun `copy with isLoading true`() {
        val state = AIAssistantUiState().copy(isLoading = true)
        assertTrue(state.isLoading)
    }

    @Test
    fun `copy with error sets error message`() {
        val state = AIAssistantUiState().copy(error = "Koneksi gagal")
        assertEquals("Koneksi gagal", state.error)
    }

    @Test
    fun `messages list is ordered`() {
        val msgs = listOf(
            ChatMessage(1L, "Pesan 1", true),
            ChatMessage(2L, "Pesan 2", false),
            ChatMessage(3L, "Pesan 3", true)
        )
        val state = AIAssistantUiState(messages = msgs)
        assertEquals("Pesan 1", state.messages[0].text)
        assertEquals("Pesan 2", state.messages[1].text)
        assertEquals("Pesan 3", state.messages[2].text)
    }

    @Test
    fun `state with input text stores correctly`() {
        val state = AIAssistantUiState(inputText = "Tips belajar efektif")
        assertEquals("Tips belajar efektif", state.inputText)
    }

    // ===== QUICK_PROMPTS =====

    @Test
    fun `QUICK_PROMPTS is not empty`() {
        assertTrue(QUICK_PROMPTS.isNotEmpty())
    }

    @Test
    fun `QUICK_PROMPTS all items are non-blank`() {
        QUICK_PROMPTS.forEach { prompt ->
            assertTrue(prompt.isNotBlank(), "Found blank prompt")
        }
    }

    @Test
    fun `QUICK_PROMPTS has at least 3 items`() {
        assertTrue(QUICK_PROMPTS.size >= 3)
    }

    @Test
    fun `QUICK_PROMPTS contains analisa prompt`() {
        assertTrue(QUICK_PROMPTS.any { it.contains("produktivitas", ignoreCase = true) || it.contains("Analisa", ignoreCase = true) })
    }
}
