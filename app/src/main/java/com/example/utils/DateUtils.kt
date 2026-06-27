package com.example.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
    private val displayMonthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))

    fun getTodayString(): String {
        return dbDateFormat.format(Date())
    }

    fun formatToDisplay(dateStr: String): String {
        return try {
            val date = dbDateFormat.parse(dateStr) ?: return dateStr
            displayDateFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatToMonthYearDisplay(year: Int, month1Based: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month1Based - 1)
        return displayMonthFormat.format(cal.time)
    }

    /**
     * Mengambil tanggal Minggu (Sunday) dan Sabtu (Saturday) dari minggu saat ini.
     * Minggu dianggap sebagai hari pertama (index 1) dan Sabtu hari terakhir (index 7).
     */
    fun getCurrentWeekRange(): Pair<String, String> {
        return getWeekRangeForDate(getTodayString())
    }

    fun getWeekRangeForDate(dateStr: String): Pair<String, String> {
        return try {
            val date = dbDateFormat.parse(dateStr) ?: Date()
            val cal = Calendar.getInstance(Locale("id", "ID"))
            cal.time = date
            
            // Set ke hari Minggu (Sunday is 1 in Calendar.SUNDAY)
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            val sundayStr = dbDateFormat.format(cal.time)

            // Tambahkan 6 hari untuk sampai ke hari Sabtu
            cal.add(Calendar.DATE, 6)
            val saturdayStr = dbDateFormat.format(cal.time)

            Pair(sundayStr, saturdayStr)
        } catch (e: Exception) {
            Pair(dateStr, dateStr)
        }
    }

    fun getMonthRange(year: Int, month1Based: Int): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month1Based - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDay = dbDateFormat.format(cal.time)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDay = dbDateFormat.format(cal.time)

        return Pair(firstDay, lastDay)
    }

    fun formatIndonesianDateRange(startStr: String, endStr: String): String {
        return try {
            val start = dbDateFormat.parse(startStr)
            val end = dbDateFormat.parse(endStr)
            val fmt = SimpleDateFormat("d MMM yyyy", Locale("id", "ID"))
            "${fmt.format(start)} s/d ${fmt.format(end)}"
        } catch (e: Exception) {
            "$startStr - $endStr"
        }
    }
}
