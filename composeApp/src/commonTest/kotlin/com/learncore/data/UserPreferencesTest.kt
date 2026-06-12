package com.learncore.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.learncore.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakePreferencesDataStore : DataStore<Preferences> {
    private val _data = MutableStateFlow<Preferences>(emptyPreferences())
    override val data: Flow<Preferences> = _data

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(_data.value)
        _data.value = updated
        return updated
    }
}

class UserPreferencesTest {

    private val fakeStore = FakePreferencesDataStore()
    private val prefs = UserPreferences(fakeStore)

    @Test
    fun `default userName is empty string`() = runTest {
        assertEquals("", prefs.userName.first())
    }

    @Test
    fun `default userStatus is Pelajar`() = runTest {
        assertEquals("Pelajar", prefs.userStatus.first())
    }

    @Test
    fun `default userEmail is empty`() = runTest {
        assertEquals("", prefs.userEmail.first())
    }

    @Test
    fun `default userBio is empty`() = runTest {
        assertEquals("", prefs.userBio.first())
    }

    @Test
    fun `default isDarkMode is false`() = runTest {
        assertFalse(prefs.isDarkMode.first())
    }

    @Test
    fun `default pomodoroWorkMinutes is 25`() = runTest {
        assertEquals(25, prefs.pomodoroWorkMinutes.first())
    }

    @Test
    fun `default pomodoroBreakMinutes is 5`() = runTest {
        assertEquals(5, prefs.pomodoroBreakMinutes.first())
    }

    @Test
    fun `default notificationsEnabled is true`() = runTest {
        assertTrue(prefs.notificationsEnabled.first())
    }

    @Test
    fun `default userPhotoUri is empty`() = runTest {
        assertEquals("", prefs.userPhotoUri.first())
    }

    @Test
    fun `setUserName persists value`() = runTest {
        prefs.setUserName("Fadhilah")
        assertEquals("Fadhilah", prefs.userName.first())
    }

    @Test
    fun `setUserStatus persists value`() = runTest {
        prefs.setUserStatus("Mahasiswa")
        assertEquals("Mahasiswa", prefs.userStatus.first())
    }

    @Test
    fun `setUserEmail persists value`() = runTest {
        prefs.setUserEmail("test@itera.ac.id")
        assertEquals("test@itera.ac.id", prefs.userEmail.first())
    }

    @Test
    fun `setUserBio persists value`() = runTest {
        prefs.setUserBio("Teknik Informatika")
        assertEquals("Teknik Informatika", prefs.userBio.first())
    }

    @Test
    fun `setDarkMode true persists`() = runTest {
        prefs.setDarkMode(true)
        assertTrue(prefs.isDarkMode.first())
    }

    @Test
    fun `setDarkMode false persists`() = runTest {
        prefs.setDarkMode(true)
        prefs.setDarkMode(false)
        assertFalse(prefs.isDarkMode.first())
    }

    @Test
    fun `setPomodoroWorkMinutes persists value`() = runTest {
        prefs.setPomodoroWorkMinutes(50)
        assertEquals(50, prefs.pomodoroWorkMinutes.first())
    }

    @Test
    fun `setPomodoroBreakMinutes persists value`() = runTest {
        prefs.setPomodoroBreakMinutes(10)
        assertEquals(10, prefs.pomodoroBreakMinutes.first())
    }

    @Test
    fun `setNotificationsEnabled false persists`() = runTest {
        prefs.setNotificationsEnabled(false)
        assertFalse(prefs.notificationsEnabled.first())
    }

    @Test
    fun `setNotificationsEnabled true persists`() = runTest {
        prefs.setNotificationsEnabled(false)
        prefs.setNotificationsEnabled(true)
        assertTrue(prefs.notificationsEnabled.first())
    }

    @Test
    fun `setUserPhotoUri persists value`() = runTest {
        prefs.setUserPhotoUri("content://media/photo.jpg")
        assertEquals("content://media/photo.jpg", prefs.userPhotoUri.first())
    }

    @Test
    fun `multiple fields can be set independently`() = runTest {
        prefs.setUserName("Fadhilah")
        prefs.setUserEmail("fadhilah@itera.ac.id")
        prefs.setDarkMode(true)
        prefs.setPomodoroWorkMinutes(45)

        assertEquals("Fadhilah", prefs.userName.first())
        assertEquals("fadhilah@itera.ac.id", prefs.userEmail.first())
        assertTrue(prefs.isDarkMode.first())
        assertEquals(45, prefs.pomodoroWorkMinutes.first())
    }

    @Test
    fun `DEFAULT_WORK_MINUTES constant is 25`() {
        assertEquals(25, UserPreferences.DEFAULT_WORK_MINUTES)
    }

    @Test
    fun `DEFAULT_BREAK_MINUTES constant is 5`() {
        assertEquals(5, UserPreferences.DEFAULT_BREAK_MINUTES)
    }

    @Test
    fun `overwrite userName replaces previous value`() = runTest {
        prefs.setUserName("Pertama")
        prefs.setUserName("Kedua")
        assertEquals("Kedua", prefs.userName.first())
    }

    @Test
    fun `overwrite pomodoroWorkMinutes replaces previous`() = runTest {
        prefs.setPomodoroWorkMinutes(30)
        prefs.setPomodoroWorkMinutes(45)
        assertEquals(45, prefs.pomodoroWorkMinutes.first())
    }
}