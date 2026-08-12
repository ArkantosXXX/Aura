package com.example.ui.focus

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.FocusSessionEntity
import com.example.data.local.entities.TaskEntity
import com.example.data.repository.FocusRepository
import com.example.data.repository.TaskRepository
import com.example.domain.model.FocusType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class FocusUiState(
    val workDurationMins: Int = 25,
    val breakDurationMins: Int = 5,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isBreak: Boolean = false,
    val selectedTaskId: String? = null,
    val tasks: List<TaskEntity> = emptyList(),
    val totalFocusMinsToday: Int = 0
)

class FocusViewModel(
    private val focusRepository: FocusRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = combine(
        _uiState,
        taskRepository.allTasks,
        focusRepository.allSessions
    ) { state, tasks, sessions ->
        val totalToday = sessions.sumOf { it.duration }
        state.copy(
            tasks = tasks.filter { !it.isCompleted },
            totalFocusMinsToday = totalToday
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusUiState()
    )

    private var timer: CountDownTimer? = null

    fun setPreset(workMins: Int, breakMins: Int) {
        timer?.cancel()
        _uiState.value = _uiState.value.copy(
            workDurationMins = workMins,
            breakDurationMins = breakMins,
            remainingSeconds = workMins * 60,
            isRunning = false,
            isBreak = false
        )
    }

    fun setSelectedTask(taskId: String?) {
        _uiState.value = _uiState.value.copy(selectedTaskId = taskId)
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return

        _uiState.value = _uiState.value.copy(isRunning = true)
        val totalMillis = _uiState.value.remainingSeconds * 1000L

        timer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = (millisUntilFinished / 1000L).toInt()
                )
            }

            override fun onFinish() {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    remainingSeconds = 0
                )
                onTimerFinished()
            }
        }.start()
    }

    fun pauseTimer() {
        timer?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timer?.cancel()
        val currentMins = if (_uiState.value.isBreak) _uiState.value.breakDurationMins else _uiState.value.workDurationMins
        _uiState.value = _uiState.value.copy(
            remainingSeconds = currentMins * 60,
            isRunning = false
        )
    }

    private fun onTimerFinished() {
        val currentState = _uiState.value
        if (!currentState.isBreak) {
            // Log focus session to DB
            viewModelScope.launch {
                focusRepository.saveSession(
                    FocusSessionEntity(
                        id = UUID.randomUUID().toString(),
                        taskId = currentState.selectedTaskId,
                        startTime = System.currentTimeMillis() - currentState.workDurationMins * 60 * 1000L,
                        endTime = System.currentTimeMillis(),
                        duration = currentState.workDurationMins,
                        breakDuration = currentState.breakDurationMins,
                        type = FocusType.POMODORO
                    )
                )
            }
            // Switch to break mode
            _uiState.value = _uiState.value.copy(
                isBreak = true,
                remainingSeconds = currentState.breakDurationMins * 60
            )
        } else {
            // Switch back to work mode
            _uiState.value = _uiState.value.copy(
                isBreak = false,
                remainingSeconds = currentState.workDurationMins * 60
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FocusViewModel(container.focusRepository, container.taskRepository) as T
        }
    }
}
