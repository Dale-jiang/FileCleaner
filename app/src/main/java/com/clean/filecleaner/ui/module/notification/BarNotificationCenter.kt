package com.clean.filecleaner.ui.module.notification

import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ui.module.SplashActivity
import com.clean.filecleaner.ui.module.notification.NotificationService.Companion.frontNoticeHasDelete
import com.clean.filecleaner.utils.AndroidVersionUtils
import com.clean.filecleaner.utils.AppPreferences
import com.clean.filecleaner.utils.AppPreferences.firstCountryCode
import java.util.Locale
import kotlin.random.Random

object BarNotificationCenter {

    private const val TAG = "BarNotificationCenter"
    private const val NOTIFICATION_CHANNEL: String = "FILE_CLEANER_MAIN"
    const val FUNCTION_TYPE = "FUNCTION_TYPE"
    const val REMINDER_TYPE = "REMINDER_TYPE"
    const val NOTICE_INFO_ITEM = "NOTICE_INFO_ITEM"

    private val firstCountry by lazy { getFirstCountryCode() }
    private val IS_KOREAN by lazy { isKorean() }

    private fun isKorean() = firstCountry == "KR" && isSamsungDevice()

    fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.equals("Samsung", ignoreCase = true)
    }

    private fun getFirstCountryCode() = run {
        var country = firstCountryCode
        if (country.isBlank()) {
            country = Locale.getDefault().country
            firstCountryCode = country
        }
        return@run country
    }


    fun init(context: Context) {
//        if (IS_KOREAN || frontNoticeHasDelete) return
        if (frontNoticeHasDelete) return
        if (context is Application && AndroidVersionUtils.isAndroid12OrAbove()) {
            updateNotice()
        } else {
            try {
                ContextCompat.startForegroundService(context, Intent(context, BarService::class.java))
            } catch (e: Exception) {
                LogUtils.e(TAG, e.message)
            }
        }
    }


    private fun getRemoteViews(isBig: Boolean): RemoteViews {
        val layoutResId = if (isBig) R.layout.layout_bar_notification else R.layout.layout_bar_notification_tiny
        val downloadSpeed = TrafficUtils.formatTraffic(TrafficUtils.currentRxSpeed)
        val uploadSpeed = TrafficUtils.formatTraffic(TrafficUtils.currentTxSpeed)

        return RemoteViews(app.packageName, layoutResId).apply {

            if (AppPreferences.networkTrafficSwitch) {
                setViewVisibility(R.id.line, View.VISIBLE)
                setViewVisibility(R.id.traffic, View.VISIBLE)
            } else {
                setViewVisibility(R.id.line, View.GONE)
                setViewVisibility(R.id.traffic, View.GONE)
            }

            setSpeedLayout(downloadSpeed, uploadSpeed)
            setOnClickPendingIntents()
        }
    }

    private fun RemoteViews.setSpeedLayout(downloadSpeed: String, uploadSpeed: String) {
        setTextViewText(R.id.speed_desc_down, "${downloadSpeed}/s")
        setTextViewText(R.id.speed_desc_up, "${uploadSpeed}/s")
    }

    private fun RemoteViews.setOnClickPendingIntents() {
        setOnClickPendingIntent(R.id.clean, toSplashPendingIntent(FuncClean))
        setOnClickPendingIntent(R.id.screenshot, toSplashPendingIntent(FuncScreenShot))
    }

    private fun toSplashPendingIntent(function: BaseBarFunction): PendingIntent {
        val intent = Intent(app, SplashActivity::class.java).apply {
            putExtra(FUNCTION_TYPE, function)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        return PendingIntent.getActivity(app, Random.nextInt(2200, 8200), intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() = run {
        NotificationManagerCompat.from(app).createNotificationChannel(
            NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setSound(null, null)
                .setLightsEnabled(false)
                .setVibrationEnabled(false)
                .setShowBadge(false)
                .setName(NOTIFICATION_CHANNEL)
                .build()
        )
    }


    @SuppressLint("MissingPermission")
    fun updateNotice(): Notification {

        createNotificationChannel()

        val deleteIntent = PendingIntent.getBroadcast(app, 0, Intent(app, BarNotificationDeleteReceiver::class.java), PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(app, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.svg_clean_tiny)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .setSound(null)
            .setDeleteIntent(deleteIntent)

        val bigRemoteViews = getRemoteViews(true)

        if (AndroidVersionUtils.isAndroid12OrAbove()) {
            val remoteViews = getRemoteViews(false)
            builder.setCustomContentView(remoteViews).setCustomBigContentView(bigRemoteViews).setCustomHeadsUpContentView(bigRemoteViews)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } else {
            builder.setCustomContentView(bigRemoteViews).setCustomBigContentView(bigRemoteViews).setCustomHeadsUpContentView(bigRemoteViews)
        }

        val notification = builder.build()

        runCatching {
            NotificationManagerCompat.from(app).notify(17777, notification)
        }

        return notification
    }


}