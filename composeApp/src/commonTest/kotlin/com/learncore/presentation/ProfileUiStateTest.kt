package com.learncore.presentation

import com.learncore.presentation.screens.profile.ProfileUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileUiStateTest {

    @Test
    fun `default userName is empty`() {
        assertEquals("", ProfileUiState().userName)
    }

    @Test
    fun `default userStatus is Pelajar`() {
        assertEquals("Pelajar", ProfileUiState().userStatus)
    }

    @Test
    fun `default userEmail is empty`() {
        assertEquals("", ProfileUiState().userEmail)
    }

    @Test
    fun `default userBio is empty`() {
        assertEquals("", ProfileUiState().userBio)
    }

    @Test
    fun `default userPhotoUri is empty`() {
        assertEquals("", ProfileUiState().userPhotoUri)
    }

    @Test
    fun `default isDarkMode is false`() {
        assertFalse(ProfileUiState().isDarkMode)
    }

    @Test
    fun `default pomodoroWorkMinutes is 25`() {
        assertEquals(25, ProfileUiState().pomodoroWorkMinutes)
    }

    @Test
    fun `default pomodoroBreakMinutes is 5`() {
        assertEquals(5, ProfileUiState().pomodoroBreakMinutes)
    }

    @Test
    fun `default notificationsEnabled is true`() {
        assertTrue(ProfileUiState().notificationsEnabled)
    }

    @Test
    fun `default isLoaded is false`() {
        assertFalse(ProfileUiState().isLoaded)
    }

    @Test
    fun `copy with userName updates correctly`() {
        val state = ProfileUiState().copy(userName = "Fadhilah")
        assertEquals("Fadhilah", state.userName)
    }

    @Test
    fun `copy with isDarkMode true`() {
        val state = ProfileUiState().copy(isDarkMode = true)
        assertTrue(state.isDarkMode)
    }

    @Test
    fun `copy with custom pomodoro work minutes`() {
        val state = ProfileUiState().copy(pomodoroWorkMinutes = 50)
        assertEquals(50, state.pomodoroWorkMinutes)
    }

    @Test
    fun `copy with custom pomodoro break minutes`() {
        val state = ProfileUiState().copy(pomodoroBreakMinutes = 10)
        assertEquals(10, state.pomodoroBreakMinutes)
    }

    @Test
    fun `copy with isLoaded true`() {
        val state = ProfileUiState().copy(isLoaded = true)
        assertTrue(state.isLoaded)
    }

    @Test
    fun `full profile state stores all fields`() {
        val state = ProfileUiState(
            userName = "Fadhilah",
            userStatus = "Mahasiswa",
            userEmail = "fadhilah@itera.ac.id",
            userBio = "Teknik Informatika ITERA",
            userPhotoUri = "content://media/photo.jpg",
            isDarkMode = true,
            pomodoroWorkMinutes = 45,
            pomodoroBreakMinutes = 10,
            notificationsEnabled = false,
            isLoaded = true
        )
        assertEquals("Fadhilah", state.userName)
        assertEquals("Mahasiswa", state.userStatus)
        assertEquals("fadhilah@itera.ac.id", state.userEmail)
        assertEquals("Teknik Informatika ITERA", state.userBio)
        assertTrue(state.isDarkMode)
        assertEquals(45, state.pomodoroWorkMinutes)
        assertEquals(10, state.pomodoroBreakMinutes)
        assertFalse(state.notificationsEnabled)
        assertTrue(state.isLoaded)
    }

    @Test
    fun `two identical states are equal`() {
        val s1 = ProfileUiState(userName = "Test")
        val s2 = ProfileUiState(userName = "Test")
        assertEquals(s1, s2)
    }

    @Test
    fun `copy preserves other fields unchanged`() {
        val state = ProfileUiState(userName = "Fadhilah", pomodoroWorkMinutes = 30)
        val updated = state.copy(isDarkMode = true)
        assertEquals("Fadhilah", updated.userName)
        assertEquals(30, updated.pomodoroWorkMinutes)
        assertTrue(updated.isDarkMode)
    }
}
