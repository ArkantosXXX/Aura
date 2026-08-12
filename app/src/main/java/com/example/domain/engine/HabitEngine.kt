package com.example.domain.engine

import com.example.data.local.entities.HabitLogEntity
import java.text.SimpleDateFormat
import java.util.*

class HabitEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Calculates current streak and longest streak for a habit without double counting logs on the same date.
     */
    fun calculateStreak(logs: List<HabitLogEntity>): StreakResult {
        if (logs.isEmpty()) return StreakResult(currentStreak = 0, longestStreak = 0, completionRate = 0f)

        // Filter completed logs and get unique dates sorted descending
        val uniqueCompletedDates = logs
            .filter { it.isCompleted }
            .map { it.date }
            .distinct()
            .sortedDescending()

        if (uniqueCompletedDates.isEmpty()) {
            return StreakResult(currentStreak = 0, longestStreak = 0, completionRate = 0f)
        }

        val todayStr = dateFormat.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        var currentStreak = 0
        var maxStreak = 0
        var tempStreak = 0

        // Check if user has log for today or yesterday to maintain active streak
        val hasLogTodayOrYesterday = uniqueCompletedDates.contains(todayStr) || uniqueCompletedDates.contains(yesterdayStr)

        if (!hasLogTodayOrYesterday) {
            currentStreak = 0
        } else {
            // Count consecutive days going backwards starting from today/yesterday
            val checkCal = Calendar.getInstance()
            if (!uniqueCompletedDates.contains(todayStr)) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1) // start from yesterday
            }

            while (true) {
                val targetDate = dateFormat.format(checkCal.time)
                if (uniqueCompletedDates.contains(targetDate)) {
                    currentStreak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }

        // Calculate longest historical streak
        val sortedAscending = uniqueCompletedDates.mapNotNull {
            try { dateFormat.parse(it) } catch (e: Exception) { null }
        }.sorted()

        if (sortedAscending.isNotEmpty()) {
            tempStreak = 1
            maxStreak = 1
            for (i in 1 until sortedAscending.size) {
                val diffDays = ((sortedAscending[i].time - sortedAscending[i - 1].time) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    tempStreak++
                    if (tempStreak > maxStreak) maxStreak = tempStreak
                } else if (diffDays > 1) {
                    tempStreak = 1
                }
            }
        }

        val rate = (uniqueCompletedDates.size.toFloat() / 30f).coerceAtMost(1.0f)

        return StreakResult(
            currentStreak = currentStreak,
            longestStreak = maxStreak.coerceAtLeast(currentStreak),
            completionRate = rate
        )
    }
}

data class StreakResult(
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float
)
