package com.learncore.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.learncore.data.local.datastore.UserPreferences
import com.learncore.presentation.screens.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeProfileDataStore : DataStore<Preferences> {
    private val _data = MutableStateFlow<Preferences>(emptyPreferences())
    override val data: Flow<Preferences> = _data
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(_data.value)
        _data.value = updated
        return updated
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ProfileViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val prefs = UserPreferences(FakeProfileDataStore())
        viewModel = ProfileViewModel(prefs)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("", state.userName)
        assertEquals("Pelajar", state.userStatus)
        assertEquals(25, state.pomodoroWorkMinutes)
        assertEquals(5, state.pomodoroBreakMinutes)
        assertTrue(state.notificationsEnabled)
        assertFalse(state.isDarkMode)
    }

    @Test
    fun `onUserNameChange updates userName in state`() = runTest {
        viewModel.onUserNameChange("Fadhilah")
        assertEquals("Fadhilah", viewModel.uiState.value.userName)
    }

    @Test
    fun `onUserStatusChange updates userStatus`() = runTest {
        viewModel.onUserStatusChange("Mahasiswa")
        assertEquals("Mahasiswa", viewModel.uiState.value.userStatus)
    }

    @Test
    fun `onUserEmailChange updates email`() = runTest {
        viewModel.onUserEmailChange("test@itera.ac.id")
        assertEquals("test@itera.ac.id", viewModel.uiState.value.userEmail)
    }

    @Test
    fun `onUserBioChange updates bio`() = runTest {
        viewModel.onUserBioChange("Teknik Informatika ITERA")
        assertEquals("Teknik Informatika ITERA", viewModel.uiState.value.userBio)
    }

    @Test
    fun `toggleDarkMode true updates state`() = runTest {
        viewModel.toggleDarkMode(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isDarkMode)
    }

    @Test
    fun `toggleDarkMode false updates state`() = runTest {
        viewModel.toggleDarkMode(true)
        advanceUntilIdle()
        viewModel.toggleDarkMode(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isDarkMode)
    }

    @Test
    fun `onWorkMinutesChange updates pomodoroWorkMinutes`() = runTest {
        viewModel.onWorkMinutesChange(45)
        advanceUntilIdle()
        assertEquals(45, viewModel.uiState.value.pomodoroWorkMinutes)
    }

    @Test
    fun `onBreakMinutesChange updates pomodoroBreakMinutes`() = runTest {
        viewModel.onBreakMinutesChange(10)
        advanceUntilIdle()
        assertEquals(10, viewModel.uiState.value.pomodoroBreakMinutes)
    }

    @Test
    fun `toggleNotifications false updates state`() = runTest {
        viewModel.toggleNotifications(false)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.notificationsEnabled)
    }

    @Test
    fun `toggleNotifications true updates state`() = runTest {
        viewModel.toggleNotifications(false)
        advanceUntilIdle()
        viewModel.toggleNotifications(true)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.notificationsEnabled)
    }

    @Test
    fun `saveProfile emits profileSaved event`() = runTest {
        var saved = false
        backgroundScope.launch { viewModel.profileSaved.collect { saved = true } }

        viewModel.onUserNameChange("Fadhilah")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertTrue(saved)
    }

    @Test
    fun `saveUserPhotoUri updates userPhotoUri in state`() = runTest {
        viewModel.saveUserPhotoUri("content://media/photo.jpg")
        advanceUntilIdle()
        assertEquals("content://media/photo.jpg", viewModel.uiState.value.userPhotoUri)
    }

    @Test
    fun `multiple field changes before save are preserved`() = runTest {
        viewModel.onUserNameChange("Fadhilah")
        viewModel.onUserStatusChange("Mahasiswa S1")
        viewModel.onUserEmailChange("fadhilah@itera.ac.id")
        viewModel.onUserBioChange("NIM 123140003")

        val state = viewModel.uiState.value
        assertEquals("Fadhilah", state.userName)
        assertEquals("Mahasiswa S1", state.userStatus)
        assertEquals("fadhilah@itera.ac.id", state.userEmail)
        assertEquals("NIM 123140003", state.userBio)
    }

    @Test
    fun `onWorkMinutesChange with custom value stores correctly`() = runTest {
        viewModel.onWorkMinutesChange(50)
        advanceUntilIdle()
        assertEquals(50, viewModel.uiState.value.pomodoroWorkMinutes)
    }

    @Test
    fun `onBreakMinutesChange does not affect work minutes`() = runTest {
        viewModel.onWorkMinutesChange(30)
        viewModel.onBreakMinutesChange(15)
        advanceUntilIdle()
        assertEquals(30, viewModel.uiState.value.pomodoroWorkMinutes)
        assertEquals(15, viewModel.uiState.value.pomodoroBreakMinutes)
    }
}