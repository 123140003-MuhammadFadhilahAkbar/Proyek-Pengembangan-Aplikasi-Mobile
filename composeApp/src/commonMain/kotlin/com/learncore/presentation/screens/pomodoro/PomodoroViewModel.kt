package com.learncore.presentation.screens.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learncore.core.notification.PomodoroNotifier
import com.learncore.data.local.datastore.UserPreferences
import com.learncore.domain.model.Task
import com.learncore.domain.usecase.GetActiveTasksUseCase
import com.learncore.domain.usecase.RecordPomodoroSessionUseCase
import com.learncore.domain.usecase.ToggleTaskCompletionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class TimerPhase { WORK, BREAK }
enum class TimerStatus { IDLE, RUNNING, PAUSED }

data class PomodoroUiState(
    val phase: TimerPhase = TimerPhase.WORK,
    val status: TimerStatus = TimerStatus.IDLE,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val sessionCount: Int = 0,
    val activeTasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,

    val showBreakBeforeNextTask: Boolean = false,
    val breakBeforeNextTaskSeconds: Int = 5 * 60,
    val breakBeforeNextTaskTotal: Int = 5 * 60
) {
    val progress: Float
        get() = if (totalSeconds == 0) 0f else remainingSeconds.toFloat() / totalSeconds

    val formattedTime: String
        get() {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }

    val breakBeforeFormattedTime: String
        get() {
            val m = breakBeforeNextTaskSeconds / 60
            val s = breakBeforeNextTaskSeconds % 60
            return "%02d:%02d".format(m, s)
        }

    val breakBeforeProgress: Float
        get() = if (breakBeforeNextTaskTotal == 0) 0f
        else breakBeforeNextTaskSeconds.toFloat() / breakBeforeNextTaskTotal
}

class PomodoroViewModel(
    private val getActiveTasksUseCase: GetActiveTasksUseCase,
    private val recordPomodoroSessionUseCase: RecordPomodoroSessionUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val userPreferences: UserPreferences
) : ViewModel(), KoinComponent {

    private val notifier: PomodoroNotifier by inject()

    private var notificationsEnabled = true

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var breakBeforeJob: Job? = null
    private var workDurationSeconds = 25 * 60
    private var breakDurationSeconds = 5 * 60

    private var savedWorkSeconds: Int? = null
    private var savedBreakSeconds: Int? = null
    private var taskToCompleteAfterBreak: Long? = null

    init {
        loadPreferences()
        loadActiveTasks()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferences.notificationsEnabled.collect { enabled ->
                notificationsEnabled = enabled
            }
        }
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
        savedWorkSeconds = null
        savedBreakSeconds = null
    }

    fun playPause() {
        when (_uiState.value.status) {
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

    fun nextPhase() {
        timerJob?.cancel()
        val state = _uiState.value
        if (state.phase == TimerPhase.WORK) {
            savedWorkSeconds = state.remainingSeconds
            viewModelScope.launch {
                recordPomodoroSessionUseCase(
                    taskId = state.selectedTask?.id,
                    durationSeconds = state.totalSeconds - state.remainingSeconds
                )
            }
            val resumeBreak = savedBreakSeconds ?: breakDurationSeconds
            _uiState.value = state.copy(
                phase = TimerPhase.BREAK,
                status = TimerStatus.IDLE,
                sessionCount = state.sessionCount + 1,
                totalSeconds = breakDurationSeconds,
                remainingSeconds = resumeBreak
            )
        } else {
            savedBreakSeconds = state.remainingSeconds
            val resumeWork = savedWorkSeconds ?: workDurationSeconds
            _uiState.value = state.copy(
                phase = TimerPhase.WORK,
                status = TimerStatus.IDLE,
                totalSeconds = workDurationSeconds,
                remainingSeconds = resumeWork
            )
        }
    }

    fun reset() {
        timerJob?.cancel()
        savedWorkSeconds = null
        savedBreakSeconds = null
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

    fun toggleTaskDone(taskId: Long) {
        viewModelScope.launch {
            toggleTaskCompletionUseCase(taskId)
        }
    }

    fun skipBreakBeforeNextTask() {
        breakBeforeJob?.cancel()
        _uiState.value = _uiState.value.copy(
            showBreakBeforeNextTask = false,
            phase = TimerPhase.WORK,
            status = TimerStatus.IDLE,
            totalSeconds = workDurationSeconds,
            remainingSeconds = workDurationSeconds
        )
    }

    private fun onBreakBeforeFinished() {
        if (notificationsEnabled) notifier.notifyBreakBeforeDone()
        _uiState.value = _uiState.value.copy(
            showBreakBeforeNextTask = false,
            phase = TimerPhase.WORK,
            status = TimerStatus.IDLE,
            totalSeconds = workDurationSeconds,
            remainingSeconds = workDurationSeconds
        )
    }

    private fun startBreakBeforeNextTask() {
        val breakSeconds = 5 * 60
        _uiState.value = _uiState.value.copy(
            showBreakBeforeNextTask = true,
            breakBeforeNextTaskSeconds = breakSeconds,
            breakBeforeNextTaskTotal = breakSeconds
        )
        breakBeforeJob = viewModelScope.launch {
            while (_uiState.value.breakBeforeNextTaskSeconds > 0) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    breakBeforeNextTaskSeconds = _uiState.value.breakBeforeNextTaskSeconds - 1
                )
            }
            onBreakBeforeFinished()
        }
    }

    private fun onTimerFinished() {
        val state = _uiState.value
        if (state.phase == TimerPhase.WORK) {
            if (notificationsEnabled) notifier.notifyWorkDone()
            viewModelScope.launch {
                recordPomodoroSessionUseCase(
                    taskId = state.selectedTask?.id,
                    durationSeconds = workDurationSeconds
                )
            }
            taskToCompleteAfterBreak = state.selectedTask?.id
            savedWorkSeconds = null
            _uiState.value = state.copy(
                phase = TimerPhase.BREAK,
                status = TimerStatus.IDLE,
                sessionCount = state.sessionCount + 1,
                totalSeconds = breakDurationSeconds,
                remainingSeconds = breakDurationSeconds
            )
            timerJob = viewModelScope.launch {
                _uiState.value = _uiState.value.copy(status = TimerStatus.RUNNING)
                while (_uiState.value.remainingSeconds > 0) {
                    delay(1000L)
                    _uiState.value = _uiState.value.copy(
                        remainingSeconds = _uiState.value.remainingSeconds - 1
                    )
                }
                onTimerFinished()
            }
        } else {
            if (notificationsEnabled) notifier.notifyBreakDone()
            savedBreakSeconds = null
            taskToCompleteAfterBreak?.let { taskId ->
                viewModelScope.launch {
                    toggleTaskCompletionUseCase(taskId)
                }
                taskToCompleteAfterBreak = null
            }
            startBreakBeforeNextTask()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        breakBeforeJob?.cancel()
    }
}
