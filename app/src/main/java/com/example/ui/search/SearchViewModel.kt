package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.HabitEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.TaskEntity
import com.example.data.repository.*
import kotlinx.coroutines.flow.*

data class SearchResults(
    val tasks: List<TaskEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList()
)

data class SearchUiState(
    val query: String = "",
    val results: SearchResults = SearchResults(),
    val selectedFilter: String = "ALL" // ALL, TASKS, NOTES, HABITS, GOALS
)

private data class SearchDataHolder(
    val tasks: List<TaskEntity>,
    val notes: List<NoteEntity>,
    val habits: List<HabitEntity>,
    val goals: List<GoalEntity>
)

class SearchViewModel(
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _filter = MutableStateFlow("ALL")

    private val queryAndFilter = combine(_query, _filter) { q, f -> Pair(q, f) }
    private val searchData = combine(
        taskRepository.allTasks,
        noteRepository.allNotes,
        habitRepository.allHabits,
        goalRepository.allGoals
    ) { tasks, notes, habits, goals ->
        SearchDataHolder(tasks, notes, habits, goals)
    }

    val uiState: StateFlow<SearchUiState> = combine(queryAndFilter, searchData) { (query, filter), data ->
        if (query.isBlank()) {
            SearchUiState(query = query, selectedFilter = filter)
        } else {
            val matchingTasks = data.tasks.filter { it.title.contains(query, ignoreCase = true) || it.desc.contains(query, ignoreCase = true) }
            val matchingNotes = data.notes.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
            val matchingHabits = data.habits.filter { it.name.contains(query, ignoreCase = true) }
            val matchingGoals = data.goals.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }

            SearchUiState(
                query = query,
                selectedFilter = filter,
                results = SearchResults(
                    tasks = if (filter == "ALL" || filter == "TASKS") matchingTasks else emptyList(),
                    notes = if (filter == "ALL" || filter == "NOTES") matchingNotes else emptyList(),
                    habits = if (filter == "ALL" || filter == "HABITS") matchingHabits else emptyList(),
                    goals = if (filter == "ALL" || filter == "GOALS") matchingGoals else emptyList()
                )
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun setQuery(q: String) {
        _query.value = q
    }

    fun setFilter(f: String) {
        _filter.value = f
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(
                container.taskRepository,
                container.noteRepository,
                container.habitRepository,
                container.goalRepository
            ) as T
        }
    }
}
