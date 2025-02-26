package com.clean.filecleaner.ui.module.notification

import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.canInteractive
import com.clean.filecleaner.ext.isOverlayPermissionGranted
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.module.notification.NotificationCenter.cleanInfos
import com.clean.filecleaner.utils.AppLifeHelper
import com.clean.filecleaner.utils.AppPreferences
import kotlinx.coroutines.launch

object FloatingWindowController {

    private val floatingWindowCenter by lazy { FloatingWindowCenter(app) }
    var windowConfig: FloatingWindowConfig? = null
    private var timerNotificationInfo: NotificationInfo? = null
    private var unlockNotificationInfo: NotificationInfo? = null

    fun displayFloatingWindow(baseReminder: BaseReminder) {
        if (canShow(baseReminder).not()) return
        postNoticeInfo(baseReminder)
        val infoItem = getNoticeInfo(baseReminder)
        NotificationService.uiScope.launch {
            adManagerState.fcLaunchState.loadAd()
            floatingWindowCenter.showFloatingWindow(infoItem)
        }
    }

    private fun postNoticeInfo(baseReminder: BaseReminder) {
        DataReportingUtils.postCustomEvent("winpop_trigger")
        when (baseReminder) {
            InstallReminder -> {
                DataReportingUtils.postCustomEvent("winpopAddTrig")
            }

            TaskReminder -> {
                DataReportingUtils.postCustomEvent("winpopTimerTrig")
            }

            UninstallReminder -> {
                DataReportingUtils.postCustomEvent("winpopUniqueTrig")
            }

            UserPresenceReminder -> {
                DataReportingUtils.postCustomEvent("winpopUnlockTrig")
            }
        }
    }


    private fun getNoticeInfo(baseReminder: BaseReminder): NotificationInfo {
        val infoItem = when (baseReminder) {
            InstallReminder -> {
                cleanInfos.filter { it.reminder == InstallReminder }.random()
            }

            TaskReminder -> {
                val notificationInfo = cleanInfos.filter { it.reminder == TaskReminder }
                    .filter { it.notificationId != timerNotificationInfo?.notificationId && it.messageId != timerNotificationInfo?.messageId }.random()
                timerNotificationInfo = notificationInfo
                notificationInfo
            }

            UninstallReminder -> {
                cleanInfos.filter { it.reminder == UninstallReminder }.random()
            }

            UserPresenceReminder -> {
                val notificationInfo = cleanInfos.filter { it.reminder == UserPresenceReminder }
                    .filter { it.notificationId != unlockNotificationInfo?.notificationId && it.messageId != unlockNotificationInfo?.messageId }.random()
                unlockNotificationInfo = notificationInfo
                notificationInfo
            }
        }
        return infoItem
    }

    private fun canShow(baseReminder: BaseReminder): Boolean {

        if (AppLifeHelper.isAppForeground() || !app.canInteractive()) {
            return false
        }

        if (null == windowConfig) {
            return false
        }

        if (windowConfig?.isOpen == 0) {
            return false
        }

        if (app.isOverlayPermissionGranted().not()) {
            return false
        }

        if (isExceedMaxCounts(baseReminder)) {
            LogUtils.d("${baseReminder.reminderName} exceed max counts")
            return false
        }
        if (isExceedInterval(baseReminder).not()) {
            LogUtils.d("${baseReminder.reminderName} no exceed interval")
            return false
        }

        LogUtils.d("${baseReminder.reminderName} can show!")
        return true
    }

    private fun isExceedMaxCounts(baseReminder: BaseReminder): Boolean {
        val max = when (baseReminder) {
            InstallReminder -> windowConfig?.installLimit ?: 0
            TaskReminder -> windowConfig?.timeLimit ?: 0
            UninstallReminder -> windowConfig?.uninstallLimit ?: 0
            UserPresenceReminder -> windowConfig?.unlockLimit ?: 0
        }
        if (0 == max) return false
        return AppPreferences.getWindowShowCounts(baseReminder) >= max
    }


    private fun isExceedInterval(baseReminder: BaseReminder): Boolean {

        val interval = when (baseReminder) {
            InstallReminder -> windowConfig?.installInterval ?: 0
            TaskReminder -> windowConfig?.timeInterval ?: 0
            UninstallReminder -> windowConfig?.uninstallInterval ?: 0
            UserPresenceReminder -> windowConfig?.unlockInterval ?: 0
        }

        if (0 == interval) return true
        return (System.currentTimeMillis() - AppPreferences.getWindowLashShow(baseReminder)) >= interval * 60000L
    }

}