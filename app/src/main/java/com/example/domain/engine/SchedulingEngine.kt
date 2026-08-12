package com.example.domain.engine

import com.example.data.local.entities.TaskEntity
import com.example.domain.model.TaskPriority
import com.example.domain.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

data class TimeSlot(val startTime: String, val endTime: String)

data class ConflictResolution(
    val recommendedTask: TaskEntity,
    val deferredTask: TaskEntity,
    val suggestedNewSlot: TimeSlot?
)

class SchedulingEngine {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Determines a non-overlapping time slot for a new or unscheduled task on a given date,
     * taking working hours, sleep hours, and existing tasks into account.
     */
    fun scheduleTask(
        task: TaskEntity,
        existingTasks: List<TaskEntity>,
        workStart: String = "09:00",
        workEnd: String = "18:00",
        sleepStart: String = "23:00",
        sleepEnd: String = "07:00"
    ): TaskEntity {
        // If task already has a startTime and doesn't conflict, return as is
        if (task.startTime.isNotBlank() && task.endTime.isNotBlank()) {
            val hasOverlap = existingTasks.any { existing ->
                existing.id != task.id &&
                existing.date == task.date &&
                existing.status != TaskStatus.CANCELLED &&
                existing.status != TaskStatus.COMPLETED &&
                isTimeOverlapping(task.startTime, task.endTime, existing.startTime, existing.endTime)
            }
            if (!hasOverlap) return task
        }

        // Find available slot within work hours
        val durationMins = task.estimatedDuration.coerceAtLeast(15)
        val sortedExisting = existingTasks
            .filter { it.date == task.date && it.startTime.isNotBlank() && it.endTime.isNotBlank() && !it.isDeleted && it.status != TaskStatus.CANCELLED }
            .sortedBy { it.startTime }

        var currentStartMins = timeToMinutes(workStart)
        val workEndMins = timeToMinutes(workEnd)

        for (existing in sortedExisting) {
            val existingStartMins = timeToMinutes(existing.startTime)
            val existingEndMins = timeToMinutes(existing.endTime)

            if (currentStartMins + durationMins <= existingStartMins) {
                // Slot found before existing task
                val startStr = minutesToTime(currentStartMins)
                val endStr = minutesToTime(currentStartMins + durationMins)
                return task.copy(
                    startTime = startStr,
                    endTime = endStr,
                    modifiedAt = System.currentTimeMillis()
                )
            } else {
                currentStartMins = currentStartMins.coerceAtLeast(existingEndMins)
            }
        }

        if (currentStartMins + durationMins <= workEndMins) {
            val startStr = minutesToTime(currentStartMins)
            val endStr = minutesToTime(currentStartMins + durationMins)
            return task.copy(
                startTime = startStr,
                endTime = endStr,
                modifiedAt = System.currentTimeMillis()
            )
        }

        // Fallback: Evening slot before sleep
        val eveningStart = workEndMins + 30
        val sleepStartMins = timeToMinutes(sleepStart)
        if (eveningStart + durationMins <= sleepStartMins) {
            val startStr = minutesToTime(eveningStart)
            val endStr = minutesToTime(eveningStart + durationMins)
            return task.copy(
                startTime = startStr,
                endTime = endStr,
                modifiedAt = System.currentTimeMillis()
            )
        }

        // If no slot on work day, assign start of work day
        return task.copy(
            startTime = workStart,
            endTime = minutesToTime(timeToMinutes(workStart) + durationMins),
            modifiedAt = System.currentTimeMillis()
        )
    }

    /**
     * Resolves time collision between two conflicting tasks based on Priority.
     */
    fun resolveConflict(task1: TaskEntity, task2: TaskEntity): ConflictResolution {
        val priorityValue1 = getPriorityWeight(task1.priority)
        val priorityValue2 = getPriorityWeight(task2.priority)

        val recommended = if (priorityValue1 >= priorityValue2) task1 else task2
        val deferred = if (priorityValue1 >= priorityValue2) task2 else task1

        val deferredDuration = deferred.estimatedDuration.coerceAtLeast(15)
        val newStartMins = timeToMinutes(recommended.endTime) + 15
        val newEndMins = newStartMins + deferredDuration

        val newSlot = TimeSlot(
            startTime = minutesToTime(newStartMins),
            endTime = minutesToTime(newEndMins)
        )

        return ConflictResolution(
            recommendedTask = recommended,
            deferredTask = deferred,
            suggestedNewSlot = newSlot
        )
    }

    /**
     * Splits a long task into smaller time blocks (e.g. 30 or 60 min chunks).
     */
    fun splitTask(task: TaskEntity, maxChunkMins: Int = 45): List<TaskEntity> {
        if (task.estimatedDuration <= maxChunkMins) return listOf(task)

        val chunkCount = (task.estimatedDuration + maxChunkMins - 1) / maxChunkMins
        val result = mutableListOf<TaskEntity>()
        var startMins = if (task.startTime.isNotBlank()) timeToMinutes(task.startTime) else 540 // 09:00

        for (i in 1..chunkCount) {
            val currentChunkMins = if (i == chunkCount) {
                task.estimatedDuration - (maxChunkMins * (chunkCount - 1))
            } else maxChunkMins

            val endMins = startMins + currentChunkMins
            result.add(
                task.copy(
                    id = if (i == 1) task.id else UUID.randomUUID().toString(),
                    title = "${task.title} (Bölüm $i/$chunkCount)",
                    estimatedDuration = currentChunkMins,
                    startTime = minutesToTime(startMins),
                    endTime = minutesToTime(endMins)
                )
            )
            startMins = endMins + 15 // 15 min buffer
        }
        return result
    }

    /**
     * Shifts a task to a new target date.
     */
    fun deferTask(task: TaskEntity, newDate: String): TaskEntity {
        return task.copy(
            date = newDate,
            isDeferred = true,
            status = TaskStatus.TODO,
            modifiedAt = System.currentTimeMillis()
        )
    }

    /**
     * Checks if a task is overdue based on current date and time.
     */
    fun checkOverdueStatus(task: TaskEntity, currentDateStr: String, currentTimeStr: String): TaskEntity {
        if (task.isCompleted || task.status == TaskStatus.CANCELLED || task.status == TaskStatus.COMPLETED) {
            return task
        }

        val isPastDate = task.date < currentDateStr
        val isSameDatePastTime = task.date == currentDateStr && task.endTime.isNotBlank() && task.endTime < currentTimeStr

        return if (isPastDate || isSameDatePastTime) {
            task.copy(status = TaskStatus.OVERDUE)
        } else {
            task
        }
    }

    private fun isTimeOverlapping(s1: String, e1: String, s2: String, e2: String): Boolean {
        val start1 = timeToMinutes(s1)
        val end1 = timeToMinutes(e1)
        val start2 = timeToMinutes(s2)
        val end2 = timeToMinutes(e2)

        return start1 < end2 && start2 < end1
    }

    private fun getPriorityWeight(priority: TaskPriority): Int {
        return when (priority) {
            TaskPriority.URGENT -> 4
            TaskPriority.HIGH -> 3
            TaskPriority.MEDIUM -> 2
            TaskPriority.LOW -> 1
        }
    }

    fun timeToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            540 // Default 09:00
        }
    }

    fun minutesToTime(minutes: Int): String {
        val hrs = (minutes / 60) % 24
        val mins = minutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hrs, mins)
    }
}
