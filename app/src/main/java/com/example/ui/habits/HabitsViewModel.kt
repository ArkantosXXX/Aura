package com.example.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.HabitEntity
import com.example.data.local.entities.HabitLogEntity
import com.example.data.repository.HabitRepository
import com.example.domain.engine.StreakResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HabitItemUiState(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val streakResult: StreakResult
)

data class HabitsUiState(
    val items: List<HabitItemUiState> = emptyList(),
    val todayDateStr: String = ""
)

class HabitsViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayStr = dateFormat.format(Date())

    val uiState: StateFlow<HabitsUiState> = combine(
        habitRepository.allHabits,
        habitRepository.getLogsForDate(todayStr)
    ) { habits, todayLogs ->

        val items = habits.map { habit ->
            val isDoneToday = todayLogs.any { it.habitId == habit.id && it.isCompleted }
            val streak = habitRepository.getHabitStreak(habit.id)
            HabitItemUiState(
                habit = habit,
                isCompletedToday = isDoneToday,
                streakResult = streak
            )
        }

        HabitsUiState(
            items = items,
            todayDateStr = todayStr
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitsUiState()
    )

    fun toggleHabitToday(habitId: String) {
        viewModelScope.launch {
            habitRepository.toggleHabitLog(habitId, todayStr)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
        }
    }

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            habitRepository.saveHabit(habit)
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HabitsViewModel(container.habitRepository) as T
        }
    }
}
