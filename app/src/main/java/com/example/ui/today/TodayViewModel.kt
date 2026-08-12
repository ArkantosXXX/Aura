package com.example.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.*
import com.example.data.repository.*
import com.example.data.sync.SyncManager
import com.example.data.sync.SyncUiState
import com.example.domain.model.DashboardCardType
import com.example.domain.model.DashboardTemplate
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TodayUiState(
    val dateStr: String = "",
    val dayNameStr: String = "",
    val greetingStr: String = "",
    val tasks: List<TaskEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val habitLogs: List<HabitLogEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val focusSessions: List<FocusSessionEntity> = emptyList(),
    val cardOrder: List<DashboardCardType> = emptyList(),
    val template: DashboardTemplate = DashboardTemplate.BALANCED,
    val syncState: SyncUiState = SyncUiState.Synced
)

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

class TodayViewModel(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository,
    private val focusRepository: FocusRepository,
    private val settingsRepository: SettingsRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayStr = dateFormat.format(Date())

    private val dayFormat = SimpleDateFormat("EEEE, d MMMM", Locale("tr"))
    private val dayNameStr = dayFormat.format(Date())

    private val coreDataFlow = combine(
        taskRepository.getTasksByDate(todayStr),
        habitRepository.allHabits,
        habitRepository.getLogsForDate(todayStr)
    ) { tasks, habits, habitLogs ->
        Triple(tasks, habits, habitLogs)
    }

    private val secondaryDataFlow = combine(
        goalRepository.allGoals,
        focusRepository.allSessions,
        settingsRepository.getDashboardConfig(),
        syncManager.syncState
    ) { goals, focusSessions, config, syncState ->
        Quadruple(goals, focusSessions, config, syncState)
    }

    val uiState: StateFlow<TodayUiState> = combine(coreDataFlow, secondaryDataFlow) { core, secondary ->
        val (tasks, habits, habitLogs) = core
        val (goals, focusSessions, config, syncState) = secondary

        val cardOrder = parseLayoutJson(config.layoutJson)

        TodayUiState(
            dateStr = todayStr,
            dayNameStr = dayNameStr,
            greetingStr = getGreetingMessage(),
            tasks = tasks,
            habits = habits,
            habitLogs = habitLogs,
            goals = goals,
            focusSessions = focusSessions,
            cardOrder = cardOrder,
            template = config.templateName,
            syncState = syncState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState()
    )

    fun toggleTask(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, isCompleted)
        }
    }

    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.toggleHabitLog(habitId, todayStr)
        }
    }

    fun applyTemplate(template: DashboardTemplate) {
        viewModelScope.launch {
            val order = when (template) {
                DashboardTemplate.MINIMAL -> listOf(
                    DashboardCardType.WELCOME,
                    DashboardCardType.TASKS,
                    DashboardCardType.TIMELINE
                )
                DashboardTemplate.STUDENT -> listOf(
                    DashboardCardType.WELCOME,
                    DashboardCardType.TASKS,
                    DashboardCardType.TIMELINE,
                    DashboardCardType.FOCUS,
                    DashboardCardType.GOALS
                )
                DashboardTemplate.PRODUCTIVITY -> listOf(
                    DashboardCardType.WELCOME,
                    DashboardCardType.PROGRESS,
                    DashboardCardType.TASKS,
                    DashboardCardType.TIMELINE,
                    DashboardCardType.FOCUS
                )
                DashboardTemplate.GOAL_FOCUSED -> listOf(
                    DashboardCardType.WELCOME,
                    DashboardCardType.GOALS,
                    DashboardCardType.PROGRESS,
                    DashboardCardType.TASKS
                )
                DashboardTemplate.HABIT_FOCUSED -> listOf(
                    DashboardCardType.WELCOME,
                    DashboardCardType.HABITS,
                    DashboardCardType.PROGRESS,
                    DashboardCardType.TASKS
                )
                DashboardTemplate.BALANCED -> listOf(
                    DashboardCardType.WELCOME,
                    DashboardCardType.PROGRESS,
                    DashboardCardType.TASKS,
                    DashboardCardType.TIMELINE,
                    DashboardCardType.HABITS,
                    DashboardCardType.GOALS,
                    DashboardCardType.FOCUS
                )
            }
            val json = listAdapter.toJson(order.map { it.name })
            settingsRepository.saveDashboardConfig(
                DashboardConfigEntity(
                    id = "dashboard_default",
                    userId = "default_user",
                    layoutJson = json,
                    templateName = template
                )
            )
        }
    }

    private fun parseLayoutJson(json: String): List<DashboardCardType> {
        return try {
            val names = listAdapter.fromJson(json) ?: emptyList()
            names.mapNotNull {
                try { DashboardCardType.valueOf(it) } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            listOf(
                DashboardCardType.WELCOME,
                DashboardCardType.PROGRESS,
                DashboardCardType.TASKS,
                DashboardCardType.TIMELINE,
                DashboardCardType.HABITS,
                DashboardCardType.GOALS,
                DashboardCardType.FOCUS
            )
        }
    }

    private fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Günaydın! Bugünü harika planlayalım."
            in 12..17 -> "Tünaydın! Verimli bir gün geçirmeye devam et."
            in 18..22 -> "İyi akşamlar! Günlük hedeflerini gözden geçir."
            else -> "İyi geceler! Dinlenme ve yarını planlama vakti."
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodayViewModel(
                container.taskRepository,
                container.habitRepository,
                container.goalRepository,
                container.focusRepository,
                container.settingsRepository,
                container.syncManager
            ) as T
        }
    }
}
