package com.reveny.habittracker.data.repository

import com.reveny.habittracker.data.local.dao.HabitDao
import com.reveny.habittracker.data.local.dao.HabitLogDao
import com.reveny.habittracker.data.local.entity.Habit
import com.reveny.habittracker.data.local.entity.HabitLog
import com.reveny.habittracker.data.model.HabitWithLogs
import com.reveny.habittracker.data.model.MonthlyComparison
import com.reveny.habittracker.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
) : HabitRepository {

    override fun getActiveHabitsWithFailures(): Flow<List<HabitWithLogs>> {
        val now = YearMonth.now()
        val prev = now.minusMonths(1)
        val (thisStart, thisEnd) = DateUtils.monthStartEnd(now)
        val (lastStart, lastEnd) = DateUtils.monthStartEnd(prev)

        return combine(
            habitDao.getActiveHabits(),
            habitLogDao.getLogsInRangeFlow(thisStart, thisEnd),
            habitLogDao.getLogsInRangeFlow(lastStart, lastEnd),
        ) { habits, thisMonthLogs, lastMonthLogs ->
            habits.map { habit ->
                HabitWithLogs(
                    habit = habit,
                    failuresThisMonth = thisMonthLogs.count { it.habitId == habit.id },
                    failuresLastMonth = lastMonthLogs.count { it.habitId == habit.id },
                )
            }
        }
    }

    override fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    override suspend fun getHabitById(id: Long): Habit? = habitDao.getById(id)

    override suspend fun createHabit(habit: Habit): Long = habitDao.insert(habit)

    override suspend fun updateHabit(habit: Habit) = habitDao.update(habit)

    override suspend fun archiveHabit(id: Long, archivedAt: String) =
        habitDao.archive(id, archivedAt)

    override suspend fun restoreHabit(id: Long) {
        val habit = habitDao.getById(id) ?: return
        habitDao.update(habit.copy(archivedAt = null))
    }

    override suspend fun deleteHabit(id: Long) = habitDao.delete(id)

    override suspend fun logFailure(habitId: Long, date: String, note: String?, failureTime: String?) {
        habitLogDao.insert(
            HabitLog(
                habitId = habitId,
                date = date,
                createdAt = DateUtils.todayIso(),
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                failureTime = normalizeFailureTimeOrNull(failureTime),
            )
        )
    }

    override suspend fun insertLogRaw(habitId: Long, date: String, note: String?, failureTime: String?): Long =
        habitLogDao.insert(
            HabitLog(
                habitId = habitId,
                date = date,
                createdAt = DateUtils.todayIso(),
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                failureTime = normalizeFailureTime(failureTime),
            )
        )

    override suspend fun removeFailure(habitId: Long, date: String) {
        habitLogDao.deleteLog(habitId, date)
    }

    override suspend fun toggleFailure(habitId: Long, date: String): Boolean {
        return if (habitLogDao.exists(habitId, date)) {
            habitLogDao.deleteLog(habitId, date)
            false // removed
        } else {
            habitLogDao.insert(
                HabitLog(
                    habitId = habitId,
                    date = date,
                    createdAt = DateUtils.todayIso(),
                    failureTime = currentFailureTime(),
                )
            )
            true // added
        }
    }

    override suspend fun hasFailure(habitId: Long, date: String): Boolean {
        return habitLogDao.exists(habitId, date)
    }

    override fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> =
        habitLogDao.getLogsForHabit(habitId)

    override suspend fun getLogsInRange(startDate: String, endDate: String): List<HabitLog> =
        habitLogDao.getLogsInRange(startDate, endDate)

    override fun getLogsInRangeFlow(startDate: String, endDate: String): Flow<List<HabitLog>> =
        habitLogDao.getLogsInRangeFlow(startDate, endDate)

    override suspend fun getMonthlyComparisons(): List<MonthlyComparison> {
        val now = YearMonth.now()
        val prev = now.minusMonths(1)
        val (thisStart, thisEnd) = DateUtils.monthStartEnd(now)
        val (lastStart, lastEnd) = DateUtils.monthStartEnd(prev)

        val habits = habitDao.getActiveHabits().first()
        val thisLogs = habitLogDao.getLogsInRange(thisStart, thisEnd)
        val lastLogs = habitLogDao.getLogsInRange(lastStart, lastEnd)

        return habits.map { habit ->
            MonthlyComparison(
                habitName = habit.name,
                thisMonthFailures = thisLogs.count { it.habitId == habit.id },
                lastMonthFailures = lastLogs.count { it.habitId == habit.id },
            )
        }
    }

    override suspend fun getAllHabitLogs(): List<HabitLog> =
        getLogsInRange("2000-01-01", "2099-12-31")

    private fun normalizeFailureTime(failureTime: String?): String =
        failureTime?.takeIf { it.matches(TIME_PATTERN) } ?: currentFailureTime()

    private fun normalizeFailureTimeOrNull(failureTime: String?): String? =
        failureTime?.takeIf { it.matches(TIME_PATTERN) }

    private fun currentFailureTime(): String =
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

    companion object {
        private val TIME_PATTERN = Regex("""\d{2}:\d{2}""")
    }
}
