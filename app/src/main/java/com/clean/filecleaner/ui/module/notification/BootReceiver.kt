package com.clean.filecleaner.ui.module.notification

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.ui.module.notification.NotificationService.Companion.frontNoticeHasDelete

class BootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (!BarNotificationCenter.isSamsungDevice() && !frontNoticeHasDelete) {
            if (null != context) {
                runCatching {
                    LogUtils.d("BootReceiver==onReceive")
                    ContextCompat.startForegroundService(context, Intent(context, BarService::class.java))
                }
            }
        }
    }

}