package com.learncore.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userName: String = "",
    val userStatus: String = "Pelajar",
    val isDarkMode: Boolean = false,
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val notificationsEnabled: Boolean = true,
    val isLoaded: Boolean = false
)

class ProfileViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            combine(
                userPreferences.userName,
                userPreferences.userStatus,
                userPreferences.isDarkMode,
                userPreferences.pomodoroWorkMinutes,
                userPreferences.pomodoroBreakMinutes,
                userPreferences.notificationsEnabled
            ) { values ->
                ProfileUiState(
                    userName = values[0] as String,
                    userStatus = values[1] as String,
                    isDarkMode = values[2] as Boolean,
                    pomodoroWorkMinutes = values[3] as Int,
                    pomodoroBreakMinutes = values[4] as Int,
                    notificationsEnabled = values[5] as Boolean,
                    isLoaded = true
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onUserNameChange(name: String) {
        _uiState.value = _uiState.value.copy(userName = name)
    }

    fun onUserStatusChange(status: String) {
        _uiState.value = _uiState.value.copy(userStatus = status)
    }

    fun saveProfile() {
        viewModelScope.launch {
            userPreferences.setUserName(_uiState.value.userName)
            userPreferences.setUserStatus(_uiState.value.userStatus)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
        viewModelScope.launch {
            userPreferences.setDarkMode(enabled)
        }
    }

    fun onWorkMinutesChange(minutes: Int) {
        _uiState.value = _uiState.value.copy(pomodoroWorkMinutes = minutes)
        viewModelScope.launch {
            userPreferences.setPomodoroWorkMinutes(minutes)
        }
    }

    fun onBreakMinutesChange(minutes: Int) {
        _uiState.value = _uiState.value.copy(pomodoroBreakMinutes = minutes)
        viewModelScope.launch {
            userPreferences.setPomodoroBreakMinutes(minutes)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }
}
