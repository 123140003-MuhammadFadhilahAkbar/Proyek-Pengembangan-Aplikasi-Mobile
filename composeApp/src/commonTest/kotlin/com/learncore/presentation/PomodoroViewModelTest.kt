package com.learncore.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.learncore.data.local.datastore.UserPreferences
import com.learncore.domain.model.EisenhowerQuadrant
import com.learncore.domain.model.ProductivityStats
import com.learncore.domain.model.Task
import com.learncore.domain.repository.TaskRepository
import com.learncore.domain.usecase.GetActiveTasksUseCase
import com.learncore.domain.usecase.RecordPomodoroSessionUseCase
import com.learncore.domain.usecase.ToggleTaskCompletionUseCase
import com.learncore.presentation.screens.pomodoro.PomodoroUiState
import com.learncore.presentation.screens.pomodoro.TimerPhase
import com.learncore.presentation.screens.pomodoro.TimerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull

class PomodoroUiStateTest {

    @Test
    fun `default state has WORK phase and IDLE status`() {
        val state = PomodoroUiState()
        assertEquals(TimerPhase.WORK, state.phase)
        assertEquals(TimerStatus.IDLE, state.status)
    }

    @Test
    fun `default totalSeconds is 25 minutes`() {
        val state = PomodoroUiState()
        assertEquals(25 * 60, state.totalSeconds)
        assertEquals(25 * 60, state.remainingSeconds)
    }

    @Test
    fun `progress is 1f when timer is full`() {
        val state = PomodoroUiState(totalSeconds = 100, remainingSeconds = 100)
        assertEquals(1f, state.progress)
    }

    @Test
    fun `progress is 0f when totalSeconds is zero`() {
        val state = PomodoroUiState(totalSeconds = 0, remainingSeconds = 0)
        assertEquals(0f, state.progress)
    }

    @Test
    fun `progress decreases correctly as time passes`() {
        val state = PomodoroUiState(totalSeconds = 100, remainingSeconds = 50)
        assertEquals(0.5f, state.progress)
    }

    @Test
    fun `formattedTime formats correctly for 25 minutes`() {
        val state = PomodoroUiState(remainingSeconds = 25 * 60)
        assertEquals("25:00", state.formattedTime)
    }

    @Test
    fun `formattedTime formats correctly for 90 seconds`() {
        val state = PomodoroUiState(remainingSeconds = 90)
        assertEquals("01:30", state.formattedTime)
    }

    @Test
    fun `formattedTime pads single digit seconds`() {
        val state = PomodoroUiState(remainingSeconds = 65)
        assertEquals("01:05", state.formattedTime)
    }

    @Test
    fun `sessionCount starts at zero`() {
        val state = PomodoroUiState()
        assertEquals(0, state.sessionCount)
    }

    @Test
    fun `selectedTask defaults to null`() {
        val state = PomodoroUiState()
        assertNull(state.selectedTask)
    }

    @Test
    fun `showBreakBeforeNextTask defaults to false`() {
        val state = PomodoroUiState()
        assertFalse(state.showBreakBeforeNextTask)
    }

    @Test
    fun `breakBeforeProgress calculates correctly`() {
        val state = PomodoroUiState(
            breakBeforeNextTaskSeconds = 150,
            breakBeforeNextTaskTotal = 300
        )
        assertEquals(0.5f, state.breakBeforeProgress)
    }

    @Test
    fun `breakBeforeProgress is 0f when total is zero`() {
        val state = PomodoroUiState(breakBeforeNextTaskTotal = 0)
        assertEquals(0f, state.breakBeforeProgress)
    }

    @Test
    fun `breakBeforeFormattedTime formats correctly`() {
        val state = PomodoroUiState(breakBeforeNextTaskSeconds = 5 * 60)
        assertEquals("05:00", state.breakBeforeFormattedTime)
    }

    @Test
    fun `state copy with RUNNING status works`() {
        val state = PomodoroUiState().copy(status = TimerStatus.RUNNING)
        assertEquals(TimerStatus.RUNNING, state.status)
    }

    @Test
    fun `state copy with BREAK phase works`() {
        val state = PomodoroUiState().copy(phase = TimerPhase.BREAK, totalSeconds = 5 * 60, remainingSeconds = 5 * 60)
        assertEquals(TimerPhase.BREAK, state.phase)
        assertEquals("05:00", state.formattedTime)
    }

    @Test
    fun `incrementing sessionCount reflected in copy`() {
        val state = PomodoroUiState(sessionCount = 3)
        assertEquals(3, state.sessionCount)
        val next = state.copy(sessionCount = state.sessionCount + 1)
        assertEquals(4, next.sessionCount)
    }
}
