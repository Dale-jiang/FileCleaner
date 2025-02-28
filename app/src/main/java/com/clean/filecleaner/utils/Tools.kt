package com.clean.filecleaner.utils

import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import android.os.storage.StorageManager
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.hasUsagePermissions
import com.clean.filecleaner.report.reporter.CloakRepository.cloakResult
import com.clean.filecleaner.report.reporter.ReferrerRepository.installReferrerStr
import com.clean.filecleaner.ui.ad.buyUserTags
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


object Tools {

    fun startTickerFlow(
        scope: CoroutineScope,
        firstDelay: Long,
        interval: Long,
        onTick: () -> Unit,
        dispatcher: CoroutineDispatcher = Dispatchers.Main
    ): Job {
        return scope.launch(dispatcher) {
            try {
                delay(firstDelay)
                flow {
                    while (isActive) {
                        emit(Unit)
                        delay(interval)
                    }
                }.flowOn(Dispatchers.IO).collect { onTick() }
            } catch (e: Exception) {
                LogUtils.e("Ticker flow encountered an error: ${e.message}")
            }
        }
    }


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


    private val storageStatsManager by lazy {
        if (AndroidVersionUtils.isAndroid8OrAbove()) {
            app.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        } else null
    }

    fun getAppDataSize(packageName: String): Long =
        if (AndroidVersionUtils.isAndroid8OrAbove()) {
            storageStatsManager?.queryStatsForPackage(StorageManager.UUID_DEFAULT, packageName, Process.myUserHandle())
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

    private val usageStatsManager by lazy {
        app.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    fun getLastUsedTime(installTime: Long, packageName: String): Long {
        if (!app.hasUsagePermissions()) {
            return 0L
        }
        return usageStatsManager?.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            installTime,
            System.currentTimeMillis()
        )?.find { it.packageName == packageName }?.lastTimeUsed ?: 0

    }


    fun isBlackUser() = cloakResult == "mackinaw"

    fun isBuyUser() = buyUserTags.any { installReferrerStr.contains(it, ignoreCase = true) }

}