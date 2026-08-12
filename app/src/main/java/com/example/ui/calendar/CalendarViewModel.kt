package com.example.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.CalendarEventEntity
import com.example.data.local.entities.TaskEntity
import com.example.data.repository.CalendarRepository
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarViewMode {
    DAILY, WEEKLY, MONTHLY
}

data class CalendarUiState(
    val viewMode: CalendarViewMode = CalendarViewMode.DAILY,
    val selectedDate: String = "",
    val events: List<CalendarEventEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList()
)

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    private val _viewMode = MutableStateFlow(CalendarViewMode.DAILY)

    val uiState: StateFlow<CalendarUiState> = combine(
        calendarRepository.allEvents,
        taskRepository.allTasks,
        _selectedDate,
        _viewMode
    ) { events, tasks, date, mode ->
        CalendarUiState(
            viewMode = mode,
            selectedDate = date,
            events = events,
            tasks = tasks.filter { it.date == date }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun addEvent(title: String, startMillis: Long, endMillis: Long) {
        viewModelScope.launch {
            calendarRepository.addEvent(
                CalendarEventEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    start = startMillis,
                    end = endMillis
                )
            )
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(container.calendarRepository, container.taskRepository) as T
        }
    }
}
