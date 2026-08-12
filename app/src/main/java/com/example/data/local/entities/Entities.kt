package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val authProvider: String,
    val createdAt: Long,
    val premiumStatus: SubscriptionStatus,
    val trialEndDate: Long?,
    val isAnonymous: Boolean
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val desc: String = "",
    val date: String, // YYYY-MM-DD
    val startTime: String = "", // HH:mm
    val endTime: String = "", // HH:mm
    val estimatedDuration: Int = 30, // minutes
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val categoryId: String? = null,
    val tags: List<String> = emptyList(),
    val note: String = "",
    val recurrenceRule: String = "", // NONE, DAILY, WEEKLY, MONTHLY
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val isDeferred: Boolean = false,
    val status: TaskStatus = TaskStatus.TODO,
    val goalId: String? = null,
    val habitId: String? = null,
    val reminderTime: Long? = null,
    val syncStatus: SyncStatusEnum = SyncStatusEnum.PENDING_CREATE,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val isDeleted: Boolean = false
)

@Entity(tableName = "task_categories")
data class TaskCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val icon: String
)

@Entity(tableName = "sub_tasks")
data class SubTaskEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val start: Long,
    val end: Long,
    val isAllDay: Boolean = false,
    val recurrence: String = "",
    val syncStatus: SyncStatusEnum = SyncStatusEnum.SYNCED
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val reminderTime: String = "09:00",
    val color: String = "#4F46E5",
    val createdAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val date: String, // YYYY-MM-DD
    val isCompleted: Boolean = true,
    val note: String = ""
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val deadline: Long? = null,
    val progress: Float = 0f, // 0.0 to 1.0
    val color: String = "#10B981",
    val parentGoalId: String? = null,
    val isDeleted: Boolean = false
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val title: String,
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "sub_goals")
data class SubGoalEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val taskId: String? = null,
    val goalId: String? = null,
    val attachmentsJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val taskId: String? = null,
    val startTime: Long,
    val endTime: Long,
    val duration: Int, // minutes
    val breakDuration: Int = 5,
    val type: FocusType = FocusType.POMODORO
)

@Entity(tableName = "dashboard_configs")
data class DashboardConfigEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val layoutJson: String,
    val templateName: DashboardTemplate = DashboardTemplate.BALANCED
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val theme: ThemeOption = ThemeOption.SYSTEM,
    val colorScheme: String = "INDIGO",
    val language: String = "tr",
    val workStart: String = "09:00",
    val workEnd: String = "18:00",
    val sleepStart: String = "23:00",
    val sleepEnd: String = "07:00",
    val notificationsEnabled: Boolean = true
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val status: SubscriptionStatus = SubscriptionStatus.FREE,
    val plan: SubscriptionPlan = SubscriptionPlan.MONTHLY,
    val startDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = System.currentTimeMillis() + 30 * 24 * 3600 * 1000L,
    val trialUsed: Boolean = false
)

@Entity(tableName = "feature_flags")
data class FeatureFlagEntity(
    @PrimaryKey val id: String,
    val key: String,
    val value: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: AnnouncementType = AnnouncementType.INFO,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val adminId: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)

@Entity(tableName = "error_reports")
data class ErrorReportEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val error: String,
    val stackTrace: String,
    val screen: String,
    val deviceInfo: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_syncs")
data class PendingSyncEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val operation: PendingOperation,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val lastRetryAt: Long = 0
)
