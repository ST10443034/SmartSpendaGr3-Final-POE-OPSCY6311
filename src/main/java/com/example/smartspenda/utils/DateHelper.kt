package com.example.smartspenda.utils

import java.text.SimpleDateFormat
import java.util.*

object DateHelper {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun timestampToString(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun timeToString(timestamp: Long?): String = if (timestamp != null) timeFormat.format(Date(timestamp)) else ""

    fun getCurrentMonthYear(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.MONTH) + 1 to cal.get(Calendar.YEAR)
    }

    fun getStartOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfMonth(month: Int, year: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return cal.timeInMillis
    }

    fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis - 1
        return start to end
    }

    fun getWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 7)
        val end = cal.timeInMillis - 1
        return start to end
    }

    fun getMonthRange(): Pair<Long, Long> {
        val (month, year) = getCurrentMonthYear()
        return getStartOfMonth(month, year) to getEndOfMonth(month, year)
    }
}