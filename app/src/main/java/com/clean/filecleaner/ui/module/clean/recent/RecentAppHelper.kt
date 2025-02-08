package com.clean.filecleaner.ui.module.clean.recent

import android.content.pm.ApplicationInfo
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object RecentAppHelper {


    fun getDateNameByIndex(index: Int): String = when (index) {

        0 -> app.getString(R.string.last60)
        1 -> {
            val todayFormatted = formatTime(System.currentTimeMillis())
            "${app.getString(R.string.today)} ($todayFormatted)"
        }

        2 -> {
            // 计算昨天凌晨时间的格式化字符串
            val yesterdayFormatted = formatTime(
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            )
            "${app.getString(R.string.yesterday)} ($yesterdayFormatted)"
        }

        3 -> app.getString(R.string.last7)

        else -> ""
    }


    private fun formatTime(time: Long): String {
        return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(time))
    }

    fun isAppEnableStop(packageName: String): Boolean {
        if (isSystemApp(packageName) || isGoogleApp(packageName)) return false
        return isAppCanStop(packageName)
    }

    private fun isAppCanStop(packageName: String): Boolean {
        return runCatching {
            val appInfo = app.packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_STOPPED) == 0
        }.getOrDefault(false)
    }

    private fun isSystemApp(packageName: String): Boolean {
        return runCatching {
            val appInfo = app.packageManager.getApplicationInfo(packageName, 0)
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        }.getOrDefault(false)
    }

    private fun isGoogleApp(packageName: String): Boolean {
        return packageName.contains("google") || packageName == "com.android.vending"
    }


    fun getDateRangePairByIndex(index: Int): Pair<Long, Long> {

        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        return when (index) {
            3 -> getStartOfDayWithOffset(-6) to currentTime // 最近 7 天
            2 -> getStartOfDayWithOffset(-1) to getEndOfDayForDate(calendar) // 昨天的 00:00 - 23:59
            1 -> getStartOfDayWithOffset(0) to currentTime // 今天的 00:00 - 当前时间
            else -> (currentTime - 60 * 60000L) to currentTime // 最近 1 小时
        }
    }

    // 获取指定偏移天数的当天 00:00 时间戳
    private fun getStartOfDayWithOffset(offset: Int): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, offset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    // 获取指定日期的当天结束时间戳（23:59:59.999）
    private fun getEndOfDayForDate(calendar: Calendar): Long {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return calendar.timeInMillis
    }


}