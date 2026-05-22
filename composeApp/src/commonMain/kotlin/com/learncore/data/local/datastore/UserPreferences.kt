package com.learncore.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_STATUS = stringPreferencesKey("user_status")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_BIO = stringPreferencesKey("user_bio")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_POMODORO_WORK_MINUTES = intPreferencesKey("pomodoro_work_minutes")
        private val KEY_POMODORO_BREAK_MINUTES = intPreferencesKey("pomodoro_break_minutes")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_USER_PHOTO_URI = stringPreferencesKey("user_photo_uri")

        const val DEFAULT_WORK_MINUTES = 25
        const val DEFAULT_BREAK_MINUTES = 5
    }

    val userName: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }

    val userStatus: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_STATUS] ?: "Pelajar"
    }

    val userEmail: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: ""
    }

    val userBio: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_BIO] ?: ""
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: false
    }

    val pomodoroWorkMinutes: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_POMODORO_WORK_MINUTES] ?: DEFAULT_WORK_MINUTES
    }

    val pomodoroBreakMinutes: Flow<Int> = dataStore.data.map { prefs ->
        prefs[KEY_POMODORO_BREAK_MINUTES] ?: DEFAULT_BREAK_MINUTES
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val userPhotoUri: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_USER_PHOTO_URI] ?: ""
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { prefs -> prefs[KEY_USER_NAME] = name }
    }

    suspend fun setUserStatus(status: String) {
        dataStore.edit { prefs -> prefs[KEY_USER_STATUS] = status }
    }

    suspend fun setUserEmail(email: String) {
        dataStore.edit { prefs -> prefs[KEY_USER_EMAIL] = email }
    }

    suspend fun setUserBio(bio: String) {
        dataStore.edit { prefs -> prefs[KEY_USER_BIO] = bio }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_DARK_MODE] = enabled }
    }

    suspend fun setPomodoroWorkMinutes(minutes: Int) {
        dataStore.edit { prefs -> prefs[KEY_POMODORO_WORK_MINUTES] = minutes }
    }

    suspend fun setPomodoroBreakMinutes(minutes: Int) {
        dataStore.edit { prefs -> prefs[KEY_POMODORO_BREAK_MINUTES] = minutes }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setUserPhotoUri(uri: String) {
        dataStore.edit { prefs -> prefs[KEY_USER_PHOTO_URI] = uri }
    }
}