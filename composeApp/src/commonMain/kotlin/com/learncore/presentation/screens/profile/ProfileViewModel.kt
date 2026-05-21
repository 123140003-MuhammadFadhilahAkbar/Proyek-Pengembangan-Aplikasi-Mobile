package com.learncore.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userName: String = "",
    val userStatus: String = "Pelajar",
    val userEmail: String = "",
    val userBio: String = "",
    val userPhotoUri: String = "",
    val isDarkMode: Boolean = false,
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val notificationsEnabled: Boolean = true,
    val isLoaded: Boolean = false
)

class ProfileViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // true saat user sedang mengetik — mencegah DataStore overwrite field yang lagi diedit
    private var isEditing = false

    // Emit Unit saat save selesai — UI pakai ini untuk tampilkan snackbar
    val profileSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        observePreferences()
    }

    /**
     * Terus collect semua flow dari DataStore secara bersamaan.
     * Kalau user sedang mengedit (isEditing=true), field teks tidak ditimpa —
     * hanya field non-teks (darkMode, pomodoro, notifs, photo) yang diupdate langsung.
     */
    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                userPreferences.userName,
                userPreferences.userStatus,
                userPreferences.userEmail,
                userPreferences.userBio,
                userPreferences.userPhotoUri,
                userPreferences.isDarkMode,
                userPreferences.pomodoroWorkMinutes,
                userPreferences.pomodoroBreakMinutes,
                userPreferences.notificationsEnabled
            ) { values ->
                val current = _uiState.value
                ProfileUiState(
                    // Kalau masih editing, pertahankan nilai yang user ketik
                    userName             = if (isEditing) current.userName  else values[0] as String,
                    userStatus           = if (isEditing) current.userStatus else values[1] as String,
                    userEmail            = if (isEditing) current.userEmail  else values[2] as String,
                    userBio              = if (isEditing) current.userBio    else values[3] as String,
                    userPhotoUri         = values[4] as String,
                    isDarkMode           = values[5] as Boolean,
                    pomodoroWorkMinutes  = values[6] as Int,
                    pomodoroBreakMinutes = values[7] as Int,
                    notificationsEnabled = values[8] as Boolean,
                    isLoaded             = true
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onUserNameChange(name: String) {
        isEditing = true
        _uiState.value = _uiState.value.copy(userName = name)
    }

    fun onUserStatusChange(status: String) {
        isEditing = true
        _uiState.value = _uiState.value.copy(userStatus = status)
    }

    fun onUserEmailChange(email: String) {
        isEditing = true
        _uiState.value = _uiState.value.copy(userEmail = email)
    }

    fun onUserBioChange(bio: String) {
        isEditing = true
        _uiState.value = _uiState.value.copy(userBio = bio)
    }

    fun saveProfile() {
        viewModelScope.launch {
            userPreferences.setUserName(_uiState.value.userName)
            userPreferences.setUserStatus(_uiState.value.userStatus)
            userPreferences.setUserEmail(_uiState.value.userEmail)
            userPreferences.setUserBio(_uiState.value.userBio)
            isEditing = false   // izinkan DataStore update state lagi
            profileSaved.emit(Unit)
        }
    }

    fun saveUserPhotoUri(uri: String) {
        _uiState.value = _uiState.value.copy(userPhotoUri = uri)
        viewModelScope.launch {
            userPreferences.setUserPhotoUri(uri)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
        viewModelScope.launch { userPreferences.setDarkMode(enabled) }
    }

    fun onWorkMinutesChange(minutes: Int) {
        _uiState.value = _uiState.value.copy(pomodoroWorkMinutes = minutes)
        viewModelScope.launch { userPreferences.setPomodoroWorkMinutes(minutes) }
    }

    fun onBreakMinutesChange(minutes: Int) {
        _uiState.value = _uiState.value.copy(pomodoroBreakMinutes = minutes)
        viewModelScope.launch { userPreferences.setPomodoroBreakMinutes(minutes) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        viewModelScope.launch { userPreferences.setNotificationsEnabled(enabled) }
    }
}