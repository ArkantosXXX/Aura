package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isDeleted = 0 ORDER BY date ASC, startTime ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date = :date AND isDeleted = 0 ORDER BY startTime ASC")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id AND isDeleted = 0")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isDeleted = 1, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun softDeleteTask(id: String, modifiedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM task_categories")
    fun getAllCategories(): Flow<List<TaskCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TaskCategoryEntity)

    @Query("SELECT * FROM sub_tasks WHERE taskId = :taskId ORDER BY orderIndex ASC")
    fun getSubTasksForTask(taskId: String): Flow<List<SubTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTaskEntity)

    @Update
    suspend fun updateSubTask(subTask: SubTaskEntity)

    @Query("DELETE FROM sub_tasks WHERE id = :subTaskId")
    suspend fun deleteSubTask(subTaskId: String)

    @Query("SELECT * FROM tasks WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR desc LIKE '%' || :query || '%')")
    fun searchTasks(query: String): Flow<List<TaskEntity>>
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isDeleted = 0")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteHabit(id: String)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    suspend fun deleteHabitLogForDate(habitId: String, date: String)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE isDeleted = 0")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity)

    @Query("UPDATE goals SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteGoal(id: String)

    @Query("SELECT * FROM milestones WHERE goalId = :goalId ORDER BY orderIndex ASC")
    fun getMilestonesForGoal(goalId: String): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity)

    @Update
    suspend fun updateMilestone(milestone: MilestoneEntity)

    @Query("SELECT * FROM sub_goals WHERE goalId = :goalId ORDER BY orderIndex ASC")
    fun getSubGoalsForGoal(goalId: String): Flow<List<SubGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubGoal(subGoal: SubGoalEntity)

    @Update
    suspend fun updateSubGoal(subGoal: SubGoalEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY modifiedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id AND isDeleted = 0")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun softDeleteNote(id: String, modifiedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllFocusSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings WHERE userId = :userId LIMIT 1")
    fun getUserSettings(userId: String): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)

    @Query("SELECT * FROM dashboard_configs WHERE userId = :userId LIMIT 1")
    fun getDashboardConfig(userId: String): Flow<DashboardConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDashboardConfig(config: DashboardConfigEntity)

    @Query("SELECT * FROM subscriptions WHERE userId = :userId LIMIT 1")
    fun getSubscription(userId: String): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSubscription(subscription: SubscriptionEntity)
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM feature_flags")
    fun getAllFeatureFlags(): Flow<List<FeatureFlagEntity>>

    @Query("SELECT * FROM feature_flags WHERE `key` = :key LIMIT 1")
    suspend fun getFeatureFlag(key: String): FeatureFlagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFeatureFlag(flag: FeatureFlagEntity)

    @Query("SELECT * FROM announcements WHERE isActive = 1 ORDER BY startDate DESC")
    fun getActiveAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    @Query("SELECT * FROM error_reports ORDER BY createdAt DESC")
    fun getAllErrorReports(): Flow<List<ErrorReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrorReport(report: ErrorReportEntity)
}

@Dao
interface SyncDao {
    @Query("SELECT * FROM pending_syncs ORDER BY createdAt ASC")
    suspend fun getAllPendingSyncs(): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSync(pendingSync: PendingSyncEntity)

    @Query("DELETE FROM pending_syncs WHERE id = :id")
    suspend fun deletePendingSync(id: String)
}

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY start ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEvent(id: String)
}
