package com.example.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.MilestoneEntity
import com.example.data.local.entities.SubGoalEntity
import com.example.data.repository.GoalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class GoalWithDetails(
    val goal: GoalEntity,
    val milestones: List<MilestoneEntity>,
    val subGoals: List<SubGoalEntity>
)

data class GoalsUiState(
    val goalsWithDetails: List<GoalWithDetails> = emptyList(),
    val isLoading: Boolean = false
)

class GoalsViewModel(
    private val goalRepository: GoalRepository
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = goalRepository.allGoals.map { goals ->
        val details = goals.map { goal ->
            val milestones = goalRepository.getMilestones(goal.id).first()
            val subGoals = goalRepository.getSubGoals(goal.id).first()
            GoalWithDetails(goal, milestones, subGoals)
        }
        GoalsUiState(goalsWithDetails = details)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState()
    )

    fun addGoal(title: String, desc: String) {
        viewModelScope.launch {
            goalRepository.saveGoal(
                GoalEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = desc
                )
            )
        }
    }

    fun saveGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalRepository.saveGoal(goal)
        }
    }

    fun addMilestone(goalId: String, title: String) {
        viewModelScope.launch {
            goalRepository.addMilestone(
                MilestoneEntity(
                    id = UUID.randomUUID().toString(),
                    goalId = goalId,
                    title = title
                )
            )
        }
    }

    fun toggleMilestone(milestone: MilestoneEntity) {
        viewModelScope.launch {
            goalRepository.toggleMilestone(milestone)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
        }
    }

    class Factory(private val container: com.example.di.AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GoalsViewModel(container.goalRepository) as T
        }
    }
}
