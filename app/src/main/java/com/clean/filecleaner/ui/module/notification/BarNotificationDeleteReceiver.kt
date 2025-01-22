package com.clean.filecleaner.ui.module.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.clean.filecleaner.ui.module.notification.NotificationService.Companion.frontNoticeHasDelete

class BarNotificationDeleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        frontNoticeHasDelete = true
    }
}