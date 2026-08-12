package com.example.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converters.Converters
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        TaskCategoryEntity::class,
        SubTaskEntity::class,
        CalendarEventEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        GoalEntity::class,
        MilestoneEntity::class,
        SubGoalEntity::class,
        NoteEntity::class,
        FocusSessionEntity::class,
        DashboardConfigEntity::class,
        UserSettingsEntity::class,
        SubscriptionEntity::class,
        FeatureFlagEntity::class,
        AnnouncementEntity::class,
        AuditLogEntity::class,
        ErrorReportEntity::class,
        PendingSyncEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun noteDao(): NoteDao
    abstract fun focusDao(): FocusDao
    abstract fun settingsDao(): SettingsDao
    abstract fun adminDao(): AdminDao
    abstract fun syncDao(): SyncDao
    abstract fun calendarDao(): CalendarDao
}
