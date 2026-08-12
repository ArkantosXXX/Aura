package com.example.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.SubTaskEntity
import com.example.data.local.entities.TaskCategoryEntity
import com.example.data.local.entities.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.domain.model.TaskPriority
import com.example.domain.model.TaskStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TasksUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val categories: List<TaskCategoryEntity> = emptyList(),
    val selectedCategoryFilter: String? = null,
    val selectedPriorityFilter: TaskPriority? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class TasksViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectedPriority = MutableStateFlow<TaskPriority?>(null)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.allTasks,
        taskRepository.allCategories,
        _selectedCategory,
        _selectedPriority,
        _searchQuery
    ) { tasks, categories, catFilter, prioFilter, query ->

        val filtered = tasks.filter { task ->
            val matchesCat = catFilter == null || task.categoryId == catFilter
            val matchesPrio = prioFilter == null || task.priority == prioFilter
            val matchesQuery = query.isBlank() || task.title.contains(query, ignoreCase = true) || task.desc.contains(query, ignoreCase = true)
            matchesCat && matchesPrio && matchesQuery
        }

        TasksUiState(
            tasks = filtered,
            categories = categories,
            selectedCategoryFilter = catFilter,
            selectedPriorityFilter = prioFilter,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState()
    )

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategory.value = categoryId
    }

    fun setPriorityFilter(priority: TaskPriority?) {
        _selectedPriority.value = priority
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTask(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, isCompleted)
        }
    }

    fun deferTaskToTomorrow(taskId: String) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tomorrowStr = dateFormat.format(cal.time)

        viewModelScope.launch {
            taskRepository.deferTask(taskId, tomorrowStr)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            val existing = taskRepository.allTasks.first()
            taskRepository.saveTask(task, existing)
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(container.taskRepository) as T
        }
    }
}
