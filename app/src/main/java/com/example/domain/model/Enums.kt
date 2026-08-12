package com.example.domain.model

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskStatus {
    TODO, IN_PROGRESS, COMPLETED, CANCELLED, OVERDUE
}

enum class HabitFrequency {
    DAILY, WEEKLY, CUSTOM
}

enum class FocusType {
    POMODORO, CUSTOM
}

enum class DashboardTemplate {
    MINIMAL, STUDENT, PRODUCTIVITY, GOAL_FOCUSED, HABIT_FOCUSED, BALANCED
}

enum class ThemeOption {
    SYSTEM, LIGHT, DARK
}

enum class SubscriptionStatus {
    FREE, TRIAL, PREMIUM, EXPIRED, CANCELLED, PENDING
}

enum class SubscriptionPlan {
    MONTHLY, YEARLY
}

enum class AnnouncementType {
    INFO, WARNING, UPDATE
}

enum class SyncStatusEnum {
    SYNCED, PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE, CONFLICT
}

enum class PendingOperation {
    CREATE, UPDATE, DELETE
}

enum class DashboardCardType {
    WELCOME, TASKS, TIMELINE, HABITS, GOALS, FOCUS, PROGRESS, UPCOMING, SUMMARY, QUICK_ADD
}
