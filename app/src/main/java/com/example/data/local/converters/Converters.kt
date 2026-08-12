package com.example.data.local.converters

import androidx.room.TypeConverter
import com.example.domain.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listStringAdapter = moshi.adapter<List<String>>(
        Types.newParameterizedType(List::class.java, String::class.java)
    )

    @TypeConverter
    fun fromListString(value: List<String>?): String {
        return listStringAdapter.toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toListString(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            listStringAdapter.fromJson(value) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromSubscriptionStatus(value: SubscriptionStatus?): String = value?.name ?: SubscriptionStatus.FREE.name

    @TypeConverter
    fun toSubscriptionStatus(value: String?): SubscriptionStatus = try {
        SubscriptionStatus.valueOf(value ?: SubscriptionStatus.FREE.name)
    } catch (e: Exception) {
        SubscriptionStatus.FREE
    }

    @TypeConverter
    fun fromTaskPriority(value: TaskPriority?): String = value?.name ?: TaskPriority.MEDIUM.name

    @TypeConverter
    fun toTaskPriority(value: String?): TaskPriority = try {
        TaskPriority.valueOf(value ?: TaskPriority.MEDIUM.name)
    } catch (e: Exception) {
        TaskPriority.MEDIUM
    }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus?): String = value?.name ?: TaskStatus.TODO.name

    @TypeConverter
    fun toTaskStatus(value: String?): TaskStatus = try {
        TaskStatus.valueOf(value ?: TaskStatus.TODO.name)
    } catch (e: Exception) {
        TaskStatus.TODO
    }

    @TypeConverter
    fun fromSyncStatusEnum(value: SyncStatusEnum?): String = value?.name ?: SyncStatusEnum.SYNCED.name

    @TypeConverter
    fun toSyncStatusEnum(value: String?): SyncStatusEnum = try {
        SyncStatusEnum.valueOf(value ?: SyncStatusEnum.SYNCED.name)
    } catch (e: Exception) {
        SyncStatusEnum.SYNCED
    }

    @TypeConverter
    fun fromHabitFrequency(value: HabitFrequency?): String = value?.name ?: HabitFrequency.DAILY.name

    @TypeConverter
    fun toHabitFrequency(value: String?): HabitFrequency = try {
        HabitFrequency.valueOf(value ?: HabitFrequency.DAILY.name)
    } catch (e: Exception) {
        HabitFrequency.DAILY
    }

    @TypeConverter
    fun fromFocusType(value: FocusType?): String = value?.name ?: FocusType.POMODORO.name

    @TypeConverter
    fun toFocusType(value: String?): FocusType = try {
        FocusType.valueOf(value ?: FocusType.POMODORO.name)
    } catch (e: Exception) {
        FocusType.POMODORO
    }

    @TypeConverter
    fun fromDashboardTemplate(value: DashboardTemplate?): String = value?.name ?: DashboardTemplate.BALANCED.name

    @TypeConverter
    fun toDashboardTemplate(value: String?): DashboardTemplate = try {
        DashboardTemplate.valueOf(value ?: DashboardTemplate.BALANCED.name)
    } catch (e: Exception) {
        DashboardTemplate.BALANCED
    }

    @TypeConverter
    fun fromThemeOption(value: ThemeOption?): String = value?.name ?: ThemeOption.SYSTEM.name

    @TypeConverter
    fun toThemeOption(value: String?): ThemeOption = try {
        ThemeOption.valueOf(value ?: ThemeOption.SYSTEM.name)
    } catch (e: Exception) {
        ThemeOption.SYSTEM
    }

    @TypeConverter
    fun fromSubscriptionPlan(value: SubscriptionPlan?): String = value?.name ?: SubscriptionPlan.MONTHLY.name

    @TypeConverter
    fun toSubscriptionPlan(value: String?): SubscriptionPlan = try {
        SubscriptionPlan.valueOf(value ?: SubscriptionPlan.MONTHLY.name)
    } catch (e: Exception) {
        SubscriptionPlan.MONTHLY
    }

    @TypeConverter
    fun fromAnnouncementType(value: AnnouncementType?): String = value?.name ?: AnnouncementType.INFO.name

    @TypeConverter
    fun toAnnouncementType(value: String?): AnnouncementType = try {
        AnnouncementType.valueOf(value ?: AnnouncementType.INFO.name)
    } catch (e: Exception) {
        AnnouncementType.INFO
    }

    @TypeConverter
    fun fromPendingOperation(value: PendingOperation?): String = value?.name ?: PendingOperation.CREATE.name

    @TypeConverter
    fun toPendingOperation(value: String?): PendingOperation = try {
        PendingOperation.valueOf(value ?: PendingOperation.CREATE.name)
    } catch (e: Exception) {
        PendingOperation.CREATE
    }
}

