package com.clean.filecleaner.ui.module.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.utils.AppPreferences
import com.clean.filecleaner.utils.Tools.startTickerFlow
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val notificationService by lazy { NotificationService() }

class NotificationService {

    companion object {
        private val bgScope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, error -> LogUtils.e("Error: ${error.message}") }) }
        val uiScope by lazy { CoroutineScope(Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, error -> LogUtils.e("Error: ${error.message}") }) }
        var frontNoticeHasDelete = false
    }

    fun initialize() {
        app.registerReceiver(systemPackageReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        })
        app.registerReceiver(userInteractionReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT)
        })
        runCatching {
            beginBackgroundTasks()
            initiateSession()
            trackTraffic()
        }
    }

    private fun trackTraffic() {
        startTickerFlow(bgScope, 1200L, 4000L, onTick = {
            if (AppPreferences.networkTrafficSwitch && !frontNoticeHasDelete) {
                TrafficUtils.updateTotalTraffic()
                BarNotificationCenter.updateNotice()
            }
        })
    }

    private fun initiateSession() {
        startTickerFlow(bgScope, 1500L, 15 * 60000L, onTick = {
            DataReportingUtils.postCustomEvent("SessBack")
        })
    }

    private fun beginBackgroundTasks() {
        startTickerFlow(bgScope, 70_000L, 120_000L, onTick = {
            DataReportingUtils.postCustomEvent("PopDetect")
            NotificationCenter.displayNotification(TaskReminder)
        })
    }

    private val userInteractionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            bgScope.launch {
                delay(1200L)
                NotificationCenter.displayNotification(UserPresenceReminder)

                if (null != context) {
                    runCatching {
                        NotificationJobIntentService.enqueueWork(context, Intent(context, NotificationJobIntentService::class.java))
                    }
                }

            }
        }
    }

    private val systemPackageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_PACKAGE_REMOVED -> {
                    bgScope.launch {
                        delay(1200L)
                        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return@launch
                        NotificationCenter.displayNotification(UninstallReminder)
                    }
                }

                Intent.ACTION_PACKAGE_ADDED -> {
                    bgScope.launch {
                        delay(1200L)
                        NotificationCenter.displayNotification(InstallReminder)
                    }
                }
            }
        }
    }

}