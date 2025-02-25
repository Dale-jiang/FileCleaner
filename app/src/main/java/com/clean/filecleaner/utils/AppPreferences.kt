package com.clean.filecleaner.utils

import android.content.Context
import android.content.SharedPreferences
import com.blankj.utilcode.util.TimeUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter
import com.clean.filecleaner.ui.module.notification.BaseReminder

object AppPreferences {

    val prefs: SharedPreferences by lazy {
        app.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    }

    var hasRequestUMP by prefs.prefDelegate(false)
    var isFirstLaunch by prefs.prefDelegate(true)
    var lastCleanCacheTime by prefs.prefDelegate(0L)
    var appInstallTime by prefs.prefDelegate(0L)
    var unusualAdClickCount by prefs.prefDelegate(0)
    var unusualAdShowCount by prefs.prefDelegate(0)
    var isUnusualUser by prefs.prefDelegate(false)

    var distinctId by prefs.prefDelegate("")

    var firstCountryCode by prefs.prefDelegate("")

    var total001Revenue by prefs.prefDelegate(0.0)
    var topPercentDatetime by prefs.prefDelegate(0L)
    var topPercentRevenue by prefs.prefDelegate(0.0)

    var networkTrafficSwitch by prefs.prefDelegate(!BarNotificationCenter.isSamsungDevice())

    var alreadySubscribedToFcm by prefs.prefDelegate(false)

    var floatingPermissionPageTime by prefs.prefDelegate(0L)


//    fun getNotificationShowCounts(baseReminder: BaseReminder): Int {
//        return if (TimeUtils.isToday(getNotificationLashShow(baseReminder))) prefs.getInt("${baseReminder.reminderName}_reminder_amounts", 0) else 0
//    }
//
//    fun getNotificationLashShow(baseReminder: BaseReminder): Long {
//        return prefs.getLong("${baseReminder.reminderName}_reminder_time", 0L)
//    }
//
//    fun updateNotificationShowCounts(baseReminder: BaseReminder) {
//        val key = "${baseReminder.reminderName}_reminder_amounts"
//        if (TimeUtils.isToday(getNotificationLashShow(baseReminder))) prefs.edit().putInt(key, prefs.getInt(key, 0) + 1).apply() else prefs.edit().putInt(key, 1).apply()
//    }
//
//    fun updateNotificationLashShow(baseReminder: BaseReminder) {
//        prefs.edit().putLong("${baseReminder.reminderName}_reminder_time", System.currentTimeMillis()).apply()
//    }

    fun getNotificationShowCounts(baseReminder: BaseReminder): Int {
        // 获取上次显示时间
        val lastShowTime = getNotificationLashShow(baseReminder)
        // 如果是今天，返回显示次数，否则返回 0
        return if (TimeUtils.isToday(lastShowTime)) {
            prefs.getInt("${baseReminder.reminderName}_reminder_amounts", 0)
        } else {
            0
        }
    }

    fun getNotificationLashShow(baseReminder: BaseReminder): Long {
        // 返回最后显示时间
        return prefs.getLong("${baseReminder.reminderName}_reminder_time", 0L)
    }

    fun updateNotificationShowCounts(baseReminder: BaseReminder) {
        val key = "${baseReminder.reminderName}_reminder_amounts"
        val lastShowTime = getNotificationLashShow(baseReminder)
        val newCount = if (TimeUtils.isToday(lastShowTime)) {
            prefs.getInt(key, 0) + 1
        } else {
            1
        }
        prefs.edit().putInt(key, newCount).apply()
    }

    fun updateNotificationLashShow(baseReminder: BaseReminder) {
        prefs.edit().putLong("${baseReminder.reminderName}_reminder_time", System.currentTimeMillis()).apply()
    }



    fun getWindowShowCounts(baseReminder: BaseReminder): Int {
        // 获取上次显示时间
        val lastShowTime = getWindowLashShow(baseReminder)
        // 如果是今天，返回显示次数，否则返回 0
        return if (TimeUtils.isToday(lastShowTime)) {
            prefs.getInt("${baseReminder.reminderName}_reminder_amounts_window", 0)
        } else {
            0
        }
    }

    fun getWindowLashShow(baseReminder: BaseReminder): Long {
        // 返回最后显示时间
        return prefs.getLong("${baseReminder.reminderName}_reminder_time_window", 0L)
    }

    fun updateNWindowShowCounts(baseReminder: BaseReminder) {
        val key = "${baseReminder.reminderName}_reminder_amounts_window"
        val lastShowTime = getNotificationLashShow(baseReminder)
        val newCount = if (TimeUtils.isToday(lastShowTime)) {
            prefs.getInt(key, 0) + 1
        } else {
            1
        }
        prefs.edit().putInt(key, newCount).apply()
    }

    fun updateWindowLashShow(baseReminder: BaseReminder) {
        prefs.edit().putLong("${baseReminder.reminderName}_reminder_time_window", System.currentTimeMillis()).apply()
    }


}