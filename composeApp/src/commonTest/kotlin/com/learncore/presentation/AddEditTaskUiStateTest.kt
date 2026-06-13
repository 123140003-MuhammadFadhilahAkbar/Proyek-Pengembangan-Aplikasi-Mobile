package com.learncore.presentation

import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.presentation.screens.tasks.AddEditTaskUiState
import com.learncore.presentation.screens.tasks.DEFAULT_CATEGORIES
import com.learncore.presentation.screens.tasks.REMINDER_OPTIONS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddEditTaskUiStateTest {

    // ===== resolvedCategory =====

    @Test
    fun `resolvedCategory returns category when not custom`() {
        val state = AddEditTaskUiState(category = "Kuliah", isCustomCategory = false)
        assertEquals("Kuliah", state.resolvedCategory)
    }

    @Test
    fun `resolvedCategory returns trimmed customCategory when custom`() {
        val state = AddEditTaskUiState(customCategory = "  Skripsi  ", isCustomCategory = true)
        assertEquals("Skripsi", state.resolvedCategory)
    }

    @Test
    fun `resolvedCategory empty when both category and custom are blank`() {
        val state = AddEditTaskUiState(category = "", customCategory = "", isCustomCategory = false)
        assertEquals("", state.resolvedCategory)
    }

    @Test
    fun `resolvedCategory custom overrides category when isCustomCategory true`() {
        val state = AddEditTaskUiState(
            category = "Kuliah",
            customCategory = "Proyek Akhir",
            isCustomCategory = true
        )
        assertEquals("Proyek Akhir", state.resolvedCategory)
    }

    // ===== Default values =====

    @Test
    fun `default title is empty`() {
        assertEquals("", AddEditTaskUiState().title)
    }

    @Test
    fun `default quadrant is DO_FIRST`() {
        assertEquals(EisenhowerQuadrant.DO_FIRST, AddEditTaskUiState().quadrant)
    }

    @Test
    fun `default isLoading is false`() {
        assertFalse(AddEditTaskUiState().isLoading)
    }

    @Test
    fun `default isSaved is false`() {
        assertFalse(AddEditTaskUiState().isSaved)
    }

    @Test
    fun `default error is null`() {
        assertNull(AddEditTaskUiState().error)
    }

    @Test
    fun `default deadlineMillis is null`() {
        assertNull(AddEditTaskUiState().deadlineMillis)
    }

    @Test
    fun `default reminderMinutes is null`() {
        assertNull(AddEditTaskUiState().reminderMinutes)
    }

    @Test
    fun `default isEditMode is false`() {
        assertFalse(AddEditTaskUiState().isEditMode)
    }

    @Test
    fun `default isCustomCategory is false`() {
        assertFalse(AddEditTaskUiState().isCustomCategory)
    }

    // ===== copy behavior =====

    @Test
    fun `copy with error preserves other fields`() {
        val state = AddEditTaskUiState(title = "Tugas", quadrant = EisenhowerQuadrant.SCHEDULE)
        val withError = state.copy(error = "Error terjadi")
        assertEquals("Tugas", withError.title)
        assertEquals(EisenhowerQuadrant.SCHEDULE, withError.quadrant)
        assertEquals("Error terjadi", withError.error)
    }

    @Test
    fun `copy with isSaved true marks as saved`() {
        val state = AddEditTaskUiState(title = "Tugas")
        val saved = state.copy(isSaved = true, isLoading = false)
        assertTrue(saved.isSaved)
        assertFalse(saved.isLoading)
    }

    // ===== DEFAULT_CATEGORIES =====

    @Test
    fun `DEFAULT_CATEGORIES contains Kuliah`() {
        assertTrue(DEFAULT_CATEGORIES.contains("Kuliah"))
    }

    @Test
    fun `DEFAULT_CATEGORIES contains Pekerjaan`() {
        assertTrue(DEFAULT_CATEGORIES.contains("Pekerjaan"))
    }

    @Test
    fun `DEFAULT_CATEGORIES contains Lifestyle`() {
        assertTrue(DEFAULT_CATEGORIES.contains("Lifestyle"))
    }

    @Test
    fun `DEFAULT_CATEGORIES has exactly 3 items`() {
        assertEquals(3, DEFAULT_CATEGORIES.size)
    }

    // ===== REMINDER_OPTIONS =====

    @Test
    fun `REMINDER_OPTIONS first item is null (no reminder)`() {
        assertNull(REMINDER_OPTIONS.first().first)
    }

    @Test
    fun `REMINDER_OPTIONS contains 15 minutes option`() {
        assertTrue(REMINDER_OPTIONS.any { it.first == 15 })
    }

    @Test
    fun `REMINDER_OPTIONS contains 1 day option`() {
        assertTrue(REMINDER_OPTIONS.any { it.first == 1440 })
    }

    @Test
    fun `REMINDER_OPTIONS has at least 5 options`() {
        assertTrue(REMINDER_OPTIONS.size >= 5)
    }

    @Test
    fun `all REMINDER_OPTIONS have non-blank label`() {
        REMINDER_OPTIONS.forEach { (_, label) ->
            assertTrue(label.isNotBlank())
        }
    }
}
