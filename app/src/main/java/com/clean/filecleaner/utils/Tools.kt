package com.clean.filecleaner.utils

import android.app.usage.StorageStatsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import android.os.storage.StorageManager
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.hasUsagePermissions
import java.util.concurrent.TimeUnit

object Tools {

    private val googleApps = arrayOf(
        "com.google.android.calendar",
        "com.android.vending",
        "com.google.android.gms",
        "com.google.android.apps.googleassistant",
        "com.google.android.googlequicksearchbox"
    )

    fun isSystemApp(packageName: String): Boolean =
        isSystemApplication(packageName) || isGoogleApplication(packageName)

    private fun isGoogleApplication(packageName: String): Boolean =
        googleApps.contains(packageName) || packageName.startsWith("com.google.android")

    private fun isSystemApplication(packageName: String): Boolean =
        runCatching {
            app.packageManager.getApplicationInfo(packageName, 0).flags and ApplicationInfo.FLAG_SYSTEM != 0
        }.getOrDefault(false)

    fun getAppDataSize(packageName: String): Long =
        if (AndroidVersionUtils.isAndroid8OrAbove()) {
            (app.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager)
                ?.queryStatsForPackage(StorageManager.UUID_DEFAULT, packageName, Process.myUserHandle())
                ?.let { it.cacheBytes + it.appBytes + it.dataBytes }
                ?: 0L
        } else {
            0L
        }

    fun calculateDaysUnused(lastUsedTime: Long): Long {
        if (lastUsedTime == 0L) {
            return Long.MAX_VALUE
        }
        val currentTime = System.currentTimeMillis()
        val diffInMillis = currentTime - lastUsedTime
        return TimeUnit.MILLISECONDS.toDays(diffInMillis)
    }

    fun getLastUsedTime(context: Context, packageName: String): Long {
        if (!context.hasUsagePermissions()) {
            return 0L
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0L
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - TimeUnit.DAYS.toMillis(30)
        val usageStatsList: List<UsageStats>? = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endTime
        )

        if (usageStatsList.isNullOrEmpty()) {
            return 0L
        }

        val usageStats: UsageStats? = usageStatsList.find { it.packageName == packageName }
        return usageStats?.lastTimeUsed ?: 0L
    }
}