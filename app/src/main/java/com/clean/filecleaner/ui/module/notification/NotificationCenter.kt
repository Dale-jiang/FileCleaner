package com.clean.filecleaner.ui.module.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DecoratedCustomViewStyle
import androidx.core.app.NotificationManagerCompat
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.isDarkMode
import com.clean.filecleaner.ext.isGrantedNotification
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.module.SplashActivity
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter.NOTICE_INFO_ITEM
import com.clean.filecleaner.utils.AndroidVersionUtils
import com.clean.filecleaner.utils.AppLifeHelper
import com.clean.filecleaner.utils.AppPreferences
import kotlinx.coroutines.launch
import kotlin.random.Random

object NotificationCenter {

    private val cleanInfos by lazy {
        listOf(
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_time_1, R.string.notice_btn_clean_time, FuncClean, TaskReminder, 12001),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_time_2, R.string.notice_btn_clean_time, FuncClean, TaskReminder, 12001),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_time_3, R.string.notice_btn_clean_time, FuncClean, TaskReminder, 12001),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_unlock_1, R.string.notice_btn_clean_unlock, FuncClean, UserPresenceReminder, 12002),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_unlock_2, R.string.notice_btn_clean_unlock, FuncClean, UserPresenceReminder, 12002),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_unlock_3, R.string.notice_btn_clean_unlock, FuncClean, UserPresenceReminder, 12002),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_unlock_4, R.string.notice_btn_clean_unlock, FuncClean, UserPresenceReminder, 12002),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_unlock_5, R.string.notice_btn_clean_unlock, FuncClean, UserPresenceReminder, 12002),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_uninstall_1, R.string.notice_btn_clean_uninstall, FuncClean, UninstallReminder, 12003),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_uninstall_2, R.string.notice_btn_clean_uninstall, FuncClean, UninstallReminder, 12003),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_uninstall_3, R.string.notice_btn_clean_uninstall, FuncClean, UninstallReminder, 12003),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_uninstall_4, R.string.notice_btn_clean_uninstall, FuncClean, UninstallReminder, 12003),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_uninstall_5, R.string.notice_btn_clean_uninstall, FuncClean, UninstallReminder, 12003),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_install_1, R.string.notice_btn_clean_install, FuncClean, InstallReminder, 12004),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_install_2, R.string.notice_btn_clean_install, FuncClean, InstallReminder, 12004),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_install_3, R.string.notice_btn_clean_install, FuncClean, InstallReminder, 12004),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_install_4, R.string.notice_btn_clean_install, FuncClean, InstallReminder, 12004),
            NotificationInfo(R.mipmap.ic_notice_clean, R.string.notice_message_clean_install_5, R.string.notice_btn_clean_install, FuncClean, InstallReminder, 12004),
        )
    }


    var noticeConfig: NotificationConfig? = null
    private const val TAG = "NotificationCenter"
    private const val NOTIFICATION_CHANNEL = "FILE_CLEANER_TOOL"

    fun canShow(baseReminder: BaseReminder): Boolean {

        if (AppLifeHelper.isAppForeground()) {
            DataReportingUtils.postCustomEvent("FGOn")
            return false
        }
        DataReportingUtils.postCustomEvent("BGOn")

        if (null == noticeConfig) {
            return false
        }

        if (noticeConfig?.isOpen == 0) {
            DataReportingUtils.postCustomEvent("SwitchOff")
            return false
        }
        DataReportingUtils.postCustomEvent("SwitchOn")

        if (app.isGrantedNotification().not()) {
            return false
        }

//        if (isKorean()) {
//            return false
//        }

//        if (isExceedFirstInstall(baseReminder).not()) {
//            "${baseWake.wakeName} no exceed first".debugLogger(TAG)
//            return@run false
//        }

        if (isExceedMaxCounts(baseReminder)) {
            LogUtils.d("${baseReminder.reminderName} exceed max counts")
            return false
        }
        if (isExceedInterval(baseReminder).not()) {
            LogUtils.d("${baseReminder.reminderName} no exceed interval")
            DataReportingUtils.postCustomEvent("IntvOff")
            return false
        }

        DataReportingUtils.postCustomEvent("IntvOn")

        LogUtils.d("${baseReminder.reminderName} can show!")
        return true
    }

    private fun isExceedMaxCounts(baseReminder: BaseReminder): Boolean {
        val max = when (baseReminder) {
            InstallReminder -> noticeConfig?.installLimit ?: 0
            TaskReminder -> noticeConfig?.timeLimit ?: 0
            UninstallReminder -> noticeConfig?.uninstallLimit ?: 0
            UserPresenceReminder -> noticeConfig?.unlockLimit ?: 0
        }
        if (0 == max) return false
        return AppPreferences.getNotificationShowCounts(baseReminder) >= max
    }


    private fun isExceedInterval(baseReminder: BaseReminder): Boolean {

        val interval = when (baseReminder) {
            InstallReminder -> noticeConfig?.installInterval ?: 0
            TaskReminder -> noticeConfig?.timeInterval ?: 0
            UninstallReminder -> noticeConfig?.uninstallInterval ?: 0
            UserPresenceReminder -> noticeConfig?.unlockInterval ?: 0
        }

        if (0 == interval) return true
        return (System.currentTimeMillis() - AppPreferences.getNotificationLashShow(baseReminder)) >= interval * 60000L
    }

//    private fun isExceedFirstInstall(baseReminder: BaseReminder) = run {
//        if (0 == item.first) return@run true
//        return@run (System.currentTimeMillis() - firstInstallTimeLong()) > item.first * 60000L
//    }


    fun displayNotification(baseReminder: BaseReminder) {
        if (canShow(baseReminder).not()) return
        postNoticeInfo(baseReminder)
        NotificationService.uiScope.launch { adManagerState.fcLaunchState.loadAd() }
        val infoItem = getNoticeInfo(baseReminder)
        showNotification(baseReminder, infoItem)
    }

    private fun postNoticeInfo(baseReminder: BaseReminder) {
        DataReportingUtils.postCustomEvent("PopAllTrig")
        when (baseReminder) {
            InstallReminder -> {
                DataReportingUtils.postCustomEvent("PopAddTrig")
            }

            TaskReminder -> {
                DataReportingUtils.postCustomEvent("PopTimerTrig")
            }

            UninstallReminder -> {
                DataReportingUtils.postCustomEvent("PopUniqueTrig")
            }

            UserPresenceReminder -> {
                DataReportingUtils.postCustomEvent("PopUnlockTrig")
            }
        }

        if (app.isDarkMode()) {
            DataReportingUtils.postCustomEvent("PopDarkTrig")
        }
    }

    private fun getNoticeInfo(baseReminder: BaseReminder): NotificationInfo {
        val infoItem = when (baseReminder) {
            InstallReminder -> {
                cleanInfos.filter { it.reminder == InstallReminder }.random()
            }

            TaskReminder -> {
                cleanInfos.filter { it.reminder == TaskReminder }.random()
            }

            UninstallReminder -> {
                cleanInfos.filter { it.reminder == UninstallReminder }.random()
            }

            UserPresenceReminder -> {
                cleanInfos.filter { it.reminder == UserPresenceReminder }.random()
            }
        }
        return infoItem
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(baseReminder: BaseReminder, infoItem: NotificationInfo) {
        createNotificationChannel()
        val pendingIntent = toSplashPendingIntent(baseReminder, infoItem)
        val builder = NotificationCompat.Builder(app, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.svg_clean_tiny)
            .setGroupSummary(false)
            .setGroup("FileCleaner")
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val bigRemoteViews = getRemoteViews(infoItem, pendingIntent, 2)
        if (AndroidVersionUtils.isAndroid12OrAbove()) {
            val remoteViews = getRemoteViews(infoItem, pendingIntent, 0)
            builder.setCustomContentView(remoteViews).setCustomHeadsUpContentView(remoteViews).setCustomBigContentView(bigRemoteViews)
            builder.setStyle(DecoratedCustomViewStyle())
        } else {
            val remoteViews = getRemoteViews(infoItem, pendingIntent, 1)
            builder.setCustomContentView(remoteViews).setCustomHeadsUpContentView(remoteViews).setCustomBigContentView(bigRemoteViews)
        }
        runCatching {
            NotificationManagerCompat.from(app).notify(infoItem.notificationId, builder.build())
            AppPreferences.updateNotificationShowCounts(baseReminder)
            AppPreferences.updateNotificationLashShow(baseReminder)
        }
    }

    private fun getRemoteViews(infoItem: NotificationInfo, pendingIntent: PendingIntent, size: Int): RemoteViews {
        val layoutResId = if (2 == size) R.layout.layout_notification_large else if (1 == size) R.layout.layout_notification_middle else R.layout.layout_notification_tiny
        return RemoteViews(app.packageName, layoutResId).apply {
            setOnClickPendingIntent(R.id.view_root, pendingIntent)
            if (2 == size) setImageViewResource(R.id.image, infoItem.icon)
            setTextViewText(R.id.message, app.getString(infoItem.messageId))
            setTextViewText(R.id.btn, app.getString(infoItem.btnStrId))
        }
    }

    private fun toSplashPendingIntent(baseReminder: BaseReminder, infoItem: NotificationInfo): PendingIntent {
        return PendingIntent.getActivity(app, Random.nextInt(2200, 8200), Intent(app, SplashActivity::class.java).apply {
//            putExtra(REMINDER_TYPE, baseReminder)
//            putExtra(FUNCTION_TYPE, infoItem.function)
            putExtra(NOTICE_INFO_ITEM, infoItem)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() = run {
        NotificationManagerCompat.from(app).createNotificationChannel(
            NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL, NotificationManagerCompat.IMPORTANCE_MAX)
                .setLightsEnabled(true)
                .setVibrationEnabled(true)
                .setShowBadge(true)
                .setName(NOTIFICATION_CHANNEL)
                .build()
        )
    }


}