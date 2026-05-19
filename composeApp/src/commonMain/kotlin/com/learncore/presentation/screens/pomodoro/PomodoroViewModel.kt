package com.learncore.presentation.screens.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.data.local.datastore.UserPreferences
import com.learncore.domain.model.Task
import com.learncore.domain.usecase.GetActiveTasksUseCase
import com.learncore.domain.usecase.RecordPomodoroSessionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class TimerPhase { WORK, BREAK }
enum class TimerStatus { IDLE, RUNNING, PAUSED }

data class PomodoroUiState(
    val phase: TimerPhase = TimerPhase.WORK,
    val status: TimerStatus = TimerStatus.IDLE,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val sessionCount: Int = 0,
    val activeTasks: List<Task> = emptyList(),
    val selectedTask: Task? = null
) {
    val progress: Float
        get() = if (totalSeconds == 0) 0f else remainingSeconds.toFloat() / totalSeconds

    val formattedTime: String
        get() {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }
}

class PomodoroViewModel(
    private val getActiveTasksUseCase: GetActiveTasksUseCase,
    private val recordPomodoroSessionUseCase: RecordPomodoroSessionUseCase,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var workDurationSeconds = 25 * 60
    private var breakDurationSeconds = 5 * 60

    init {
        loadPreferences()
        loadActiveTasks()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferences.pomodoroWorkMinutes.collect { minutes ->
                workDurationSeconds = minutes * 60
                if (_uiState.value.phase == TimerPhase.WORK && _uiState.value.status == TimerStatus.IDLE) {
                    _uiState.value = _uiState.value.copy(
                        totalSeconds = workDurationSeconds,
                        remainingSeconds = workDurationSeconds
                    )
                }
            }
        }
        viewModelScope.launch {
            userPreferences.pomodoroBreakMinutes.collect { minutes ->
                breakDurationSeconds = minutes * 60
            }
        }
    }

    private fun loadActiveTasks() {
        viewModelScope.launch {
            getActiveTasksUseCase().collect { tasks ->
                _uiState.value = _uiState.value.copy(activeTasks = tasks)
            }
        }
    }

    fun selectTask(task: Task?) {
        _uiState.value = _uiState.value.copy(selectedTask = task)
    }

    fun playPause() {
        val state = _uiState.value
        when (state.status) {
            TimerStatus.IDLE, TimerStatus.PAUSED -> startTimer()
            TimerStatus.RUNNING -> pauseTimer()
        }
    }

    private fun startTimer() {
        _uiState.value = _uiState.value.copy(status = TimerStatus.RUNNING)
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = _uiState.value.remainingSeconds - 1
                )
            }
            onTimerFinished()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(status = TimerStatus.PAUSED)
    }

    fun reset() {
        timerJob?.cancel()
        val duration = if (_uiState.value.phase == TimerPhase.WORK) {
            workDurationSeconds
        } else {
            breakDurationSeconds
        }
        _uiState.value = _uiState.value.copy(
            status = TimerStatus.IDLE,
            remainingSeconds = duration,
            totalSeconds = duration
        )
    }

    private fun onTimerFinished() {
        val state = _uiState.value
        if (state.phase == TimerPhase.WORK) {
            // Record completed session
            viewModelScope.launch {
                recordPomodoroSessionUseCase(
                    taskId = state.selectedTask?.id,
                    durationSeconds = workDurationSeconds
                )
            }
            // Switch to break
            val newSession = state.sessionCount + 1
            _uiState.value = state.copy(
                phase = TimerPhase.BREAK,
                status = TimerStatus.IDLE,
                sessionCount = newSession,
                totalSeconds = breakDurationSeconds,
                remainingSeconds = breakDurationSeconds
            )
        } else {
            // Switch back to work
            _uiState.value = state.copy(
                phase = TimerPhase.WORK,
                status = TimerStatus.IDLE,
                totalSeconds = workDurationSeconds,
                remainingSeconds = workDurationSeconds
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
