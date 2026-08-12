package com.example.data.repository

import com.example.data.local.dao.*
import com.example.data.local.entities.*
import com.example.data.sync.SyncManager
import com.example.domain.engine.GoalEngine
import com.example.domain.engine.HabitEngine
import com.example.domain.engine.SchedulingEngine
import com.example.domain.engine.StreakResult
import com.example.domain.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class TaskRepository(
    private val taskDao: TaskDao,
    private val schedulingEngine: SchedulingEngine,
    private val syncManager: SyncManager
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allCategories: Flow<List<TaskCategoryEntity>> = taskDao.getAllCategories()

    fun getTasksByDate(date: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksByDate(date)
    }

    fun getSubTasks(taskId: String): Flow<List<SubTaskEntity>> {
        return taskDao.getSubTasksForTask(taskId)
    }

    suspend fun getTaskById(id: String): TaskEntity? {
        return taskDao.getTaskById(id)
    }

    suspend fun saveTask(task: TaskEntity, existingTasks: List<TaskEntity> = emptyList()) {
        val scheduledTask = schedulingEngine.scheduleTask(task, existingTasks)
        taskDao.insertTask(scheduledTask)
        syncManager.enqueueSync("TASK", scheduledTask.id, PendingOperation.CREATE, "{}")
    }

    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        val task = taskDao.getTaskById(taskId) ?: return
        val updated = task.copy(
            isCompleted = isCompleted,
            status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.TODO,
            modifiedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
        syncManager.enqueueSync("TASK", taskId, PendingOperation.UPDATE, "{}")
    }

    suspend fun deferTask(taskId: String, newDate: String) {
        val task = taskDao.getTaskById(taskId) ?: return
        val deferred = schedulingEngine.deferTask(task, newDate)
        taskDao.updateTask(deferred)
        syncManager.enqueueSync("TASK", taskId, PendingOperation.UPDATE, "{}")
    }

    suspend fun deleteTask(id: String) {
        taskDao.softDeleteTask(id)
        syncManager.enqueueSync("TASK", id, PendingOperation.DELETE, "{}")
    }

    suspend fun addCategory(category: TaskCategoryEntity) {
        taskDao.insertCategory(category)
    }

    suspend fun addSubTask(subTask: SubTaskEntity) {
        taskDao.insertSubTask(subTask)
    }

    suspend fun toggleSubTask(subTask: SubTaskEntity) {
        taskDao.updateSubTask(subTask.copy(isCompleted = !subTask.isCompleted))
    }

    fun searchTasks(query: String): Flow<List<TaskEntity>> {
        return taskDao.searchTasks(query)
    }
}

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitEngine: HabitEngine,
    private val syncManager: SyncManager
) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    fun getLogsForDate(date: String): Flow<List<HabitLogEntity>> {
        return habitDao.getLogsForDate(date)
    }

    suspend fun saveHabit(habit: HabitEntity) {
        habitDao.insertHabit(habit)
        syncManager.enqueueSync("HABIT", habit.id, PendingOperation.CREATE, "{}")
    }

    suspend fun deleteHabit(id: String) {
        habitDao.softDeleteHabit(id)
        syncManager.enqueueSync("HABIT", id, PendingOperation.DELETE, "{}")
    }

    suspend fun toggleHabitLog(habitId: String, date: String) {
        val logs = habitDao.getLogsForDate(date).first()
        val existing = logs.find { it.habitId == habitId }

        if (existing != null) {
            habitDao.deleteHabitLogForDate(habitId, date)
        } else {
            val log = HabitLogEntity(
                id = UUID.randomUUID().toString(),
                habitId = habitId,
                date = date,
                isCompleted = true
            )
            habitDao.insertHabitLog(log)
        }
        syncManager.enqueueSync("HABIT_LOG", habitId, PendingOperation.UPDATE, "{}")
    }

    suspend fun getHabitStreak(habitId: String): StreakResult {
        val logs = habitDao.getLogsForHabit(habitId).first()
        return habitEngine.calculateStreak(logs)
    }
}

class GoalRepository(
    private val goalDao: GoalDao,
    private val goalEngine: GoalEngine,
    private val syncManager: SyncManager
) {
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    fun getMilestones(goalId: String): Flow<List<MilestoneEntity>> {
        return goalDao.getMilestonesForGoal(goalId)
    }

    fun getSubGoals(goalId: String): Flow<List<SubGoalEntity>> {
        return goalDao.getSubGoalsForGoal(goalId)
    }

    suspend fun saveGoal(goal: GoalEntity) {
        goalDao.insertGoal(goal)
        syncManager.enqueueSync("GOAL", goal.id, PendingOperation.CREATE, "{}")
    }

    suspend fun deleteGoal(id: String) {
        goalDao.softDeleteGoal(id)
        syncManager.enqueueSync("GOAL", id, PendingOperation.DELETE, "{}")
    }

    suspend fun addMilestone(milestone: MilestoneEntity) {
        goalDao.insertMilestone(milestone)
        updateGoalProgress(milestone.goalId)
    }

    suspend fun toggleMilestone(milestone: MilestoneEntity) {
        val updated = milestone.copy(isCompleted = !milestone.isCompleted)
        goalDao.updateMilestone(updated)
        updateGoalProgress(milestone.goalId)
    }

    suspend fun addSubGoal(subGoal: SubGoalEntity) {
        goalDao.insertSubGoal(subGoal)
        updateGoalProgress(subGoal.goalId)
    }

    suspend fun toggleSubGoal(subGoal: SubGoalEntity) {
        val updated = subGoal.copy(isCompleted = !subGoal.isCompleted)
        goalDao.updateSubGoal(updated)
        updateGoalProgress(subGoal.goalId)
    }

    private suspend fun updateGoalProgress(goalId: String) {
        val goals = goalDao.getAllGoals().first()
        val goal = goals.find { it.id == goalId } ?: return
        val milestones = goalDao.getMilestonesForGoal(goalId).first()
        val subGoals = goalDao.getSubGoalsForGoal(goalId).first()

        val progress = goalEngine.calculateProgress(milestones, subGoals)
        goalDao.insertGoal(goal.copy(progress = progress))
    }
}

class NoteRepository(
    private val noteDao: NoteDao,
    private val syncManager: SyncManager
) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun getNoteById(id: String): NoteEntity? {
        return noteDao.getNoteById(id)
    }

    suspend fun saveNote(note: NoteEntity) {
        noteDao.insertNote(note)
        syncManager.enqueueSync("NOTE", note.id, PendingOperation.CREATE, "{}")
    }

    suspend fun deleteNote(id: String) {
        noteDao.softDeleteNote(id)
        syncManager.enqueueSync("NOTE", id, PendingOperation.DELETE, "{}")
    }

    fun searchNotes(query: String): Flow<List<NoteEntity>> {
        return noteDao.searchNotes(query)
    }
}

class FocusRepository(
    private val focusDao: FocusDao
) {
    val allSessions: Flow<List<FocusSessionEntity>> = focusDao.getAllFocusSessions()

    suspend fun saveSession(session: FocusSessionEntity) {
        focusDao.insertFocusSession(session)
    }
}

class SettingsRepository(
    private val settingsDao: SettingsDao
) {
    fun getUserSettings(userId: String = "default_user"): Flow<UserSettingsEntity> {
        return settingsDao.getUserSettings(userId).map { entity ->
            entity ?: UserSettingsEntity(
                id = "settings_default",
                userId = userId,
                theme = ThemeOption.SYSTEM,
                language = "tr",
                workStart = "09:00",
                workEnd = "18:00"
            )
        }
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        settingsDao.saveUserSettings(settings)
    }

    fun getDashboardConfig(userId: String = "default_user"): Flow<DashboardConfigEntity> {
        return settingsDao.getDashboardConfig(userId).map { entity ->
            entity ?: DashboardConfigEntity(
                id = "dashboard_default",
                userId = userId,
                layoutJson = "[\"WELCOME\",\"PROGRESS\",\"TASKS\",\"TIMELINE\",\"HABITS\",\"GOALS\",\"FOCUS\"]",
                templateName = DashboardTemplate.BALANCED
            )
        }
    }

    suspend fun saveDashboardConfig(config: DashboardConfigEntity) {
        settingsDao.saveDashboardConfig(config)
    }
}

class SubscriptionRepository(
    private val settingsDao: SettingsDao
) {
    fun getSubscription(userId: String = "default_user"): Flow<SubscriptionEntity> {
        return settingsDao.getSubscription(userId).map { entity ->
            entity ?: SubscriptionEntity(
                id = "sub_default",
                userId = userId,
                status = SubscriptionStatus.FREE,
                plan = SubscriptionPlan.MONTHLY
            )
        }
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        settingsDao.saveSubscription(subscription)
    }

    suspend fun activateTrial(userId: String = "default_user"): Boolean {
        val current = settingsDao.getSubscription(userId).firstOrNull()
        if (current?.trialUsed == true) {
            return false // Prevent reusing trial!
        }

        val trialSub = SubscriptionEntity(
            id = "sub_trial",
            userId = userId,
            status = SubscriptionStatus.TRIAL,
            plan = SubscriptionPlan.MONTHLY,
            startDate = System.currentTimeMillis(),
            expiryDate = System.currentTimeMillis() + 7 * 24 * 3600 * 1000L, // 7 days trial
            trialUsed = true
        )
        settingsDao.saveSubscription(trialSub)
        return true
    }
}

class PremiumGate(private val subscriptionRepository: SubscriptionRepository) {
    suspend fun isPremiumUser(userId: String = "default_user"): Boolean {
        val sub = subscriptionRepository.getSubscription(userId).first()
        val now = System.currentTimeMillis()
        return (sub.status == SubscriptionStatus.PREMIUM || sub.status == SubscriptionStatus.TRIAL) && sub.expiryDate > now
    }
}

class AdminRepository(
    private val adminDao: AdminDao
) {
    val featureFlags: Flow<List<FeatureFlagEntity>> = adminDao.getAllFeatureFlags()
    val announcements: Flow<List<AnnouncementEntity>> = adminDao.getActiveAnnouncements()
    val auditLogs: Flow<List<AuditLogEntity>> = adminDao.getAllAuditLogs()
    val errorReports: Flow<List<ErrorReportEntity>> = adminDao.getAllErrorReports()

    suspend fun setFeatureFlag(key: String, enabled: Boolean) {
        adminDao.saveFeatureFlag(FeatureFlagEntity(id = key, key = key, value = enabled))
    }

    suspend fun addAnnouncement(announcement: AnnouncementEntity) {
        adminDao.insertAnnouncement(announcement)
    }

    suspend fun logAudit(adminId: String, action: String, target: String, details: String) {
        val log = AuditLogEntity(
            id = UUID.randomUUID().toString(),
            adminId = adminId,
            action = action,
            target = target,
            details = details
        )
        adminDao.insertAuditLog(log)
    }

    suspend fun reportError(userId: String, error: String, stackTrace: String, screen: String) {
        val report = ErrorReportEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            error = error,
            stackTrace = stackTrace,
            screen = screen,
            deviceInfo = "Android SDK ${android.os.Build.VERSION.SDK_INT}"
        )
        adminDao.insertErrorReport(report)
    }
}

class CalendarRepository(
    private val calendarDao: CalendarDao
) {
    val allEvents: Flow<List<CalendarEventEntity>> = calendarDao.getAllEvents()

    suspend fun addEvent(event: CalendarEventEntity) {
        calendarDao.insertEvent(event)
    }

    suspend fun deleteEvent(id: String) {
        calendarDao.deleteEvent(id)
    }
}
