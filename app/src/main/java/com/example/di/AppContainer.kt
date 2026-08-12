package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.database.AppDatabase
import com.example.data.local.entities.TaskCategoryEntity
import com.example.data.repository.*
import com.example.data.sync.NetworkMonitor
import com.example.data.sync.SyncManager
import com.example.domain.engine.GoalEngine
import com.example.domain.engine.HabitEngine
import com.example.domain.engine.SchedulingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContainer(private val context: Context) {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "aura_plan_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val schedulingEngine: SchedulingEngine by lazy { SchedulingEngine() }
    val habitEngine: HabitEngine by lazy { HabitEngine() }
    val goalEngine: GoalEngine by lazy { GoalEngine() }

    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(context.applicationContext) }
    val syncManager: SyncManager by lazy {
        SyncManager(
            syncDao = database.syncDao(),
            networkMonitor = networkMonitor,
            scope = applicationScope
        )
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(
            taskDao = database.taskDao(),
            schedulingEngine = schedulingEngine,
            syncManager = syncManager
        )
    }

    val habitRepository: HabitRepository by lazy {
        HabitRepository(
            habitDao = database.habitDao(),
            habitEngine = habitEngine,
            syncManager = syncManager
        )
    }

    val goalRepository: GoalRepository by lazy {
        GoalRepository(
            goalDao = database.goalDao(),
            goalEngine = goalEngine,
            syncManager = syncManager
        )
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepository(
            noteDao = database.noteDao(),
            syncManager = syncManager
        )
    }

    val focusRepository: FocusRepository by lazy {
        FocusRepository(focusDao = database.focusDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(settingsDao = database.settingsDao())
    }

    val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepository(settingsDao = database.settingsDao())
    }

    val premiumGate: PremiumGate by lazy {
        PremiumGate(subscriptionRepository = subscriptionRepository)
    }

    val adminRepository: AdminRepository by lazy {
        AdminRepository(adminDao = database.adminDao())
    }

    val calendarRepository: CalendarRepository by lazy {
        CalendarRepository(calendarDao = database.calendarDao())
    }

    init {
        // Seed default categories and feature flags if first run
        applicationScope.launch {
            seedDefaultData()
        }
    }

    private suspend fun seedDefaultData() {
        val categories = listOf(
            TaskCategoryEntity("cat_work", "İş & Kariyer", "#4F46E5", "work"),
            TaskCategoryEntity("cat_personal", "Kişisel Yaşam", "#10B981", "person"),
            TaskCategoryEntity("cat_health", "Sağlık & Spor", "#EF4444", "favorite"),
            TaskCategoryEntity("cat_finance", "Finans & Bütçe", "#F59E0B", "payments"),
            TaskCategoryEntity("cat_education", "Eğitim & Öğrenme", "#8B5CF6", "school")
        )
        for (cat in categories) {
            database.taskDao().insertCategory(cat)
        }

        // Seed default feature flags
        adminRepository.setFeatureFlag("feature_focus", true)
        adminRepository.setFeatureFlag("feature_google_calendar", true)
        adminRepository.setFeatureFlag("feature_premium_widgets", true)
        adminRepository.setFeatureFlag("feature_voice_input", true)
        adminRepository.setFeatureFlag("feature_new_dashboard", true)
        adminRepository.setFeatureFlag("feature_data_export", true)
    }
}
