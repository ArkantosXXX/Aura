package com.example.domain.engine

import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.MilestoneEntity
import com.example.data.local.entities.SubGoalEntity

class GoalEngine {

    /**
     * Calculates the completion progress (0.0 to 1.0) of a goal based on its Milestones and SubGoals.
     */
    fun calculateProgress(
        milestones: List<MilestoneEntity>,
        subGoals: List<SubGoalEntity>
    ): Float {
        val totalItems = milestones.size + subGoals.size
        if (totalItems == 0) return 0f

        val completedMilestones = milestones.count { it.isCompleted }
        val completedSubGoals = subGoals.count { it.isCompleted }

        return (completedMilestones + completedSubGoals).toFloat() / totalItems.toFloat()
    }
}
