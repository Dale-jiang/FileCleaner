package com.clean.filecleaner.ui.module.notification

import android.content.Context
import android.content.Intent
import androidx.core.app.TempJobIntentService
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.ext.isGrantedNotification
import com.clean.filecleaner.ui.module.notification.NotificationService.Companion.frontNoticeHasDelete

class NotificationJobIntentService : TempJobIntentService() {

    override fun onHandleWork(intent: Intent) {

        if (!BarNotificationCenter.isSamsungDevice() && !frontNoticeHasDelete) {
            runCatching {
                LogUtils.d("onHandleWork")
                ContextCompat.startForegroundService(this, Intent(this, BarService::class.java))
            }.onFailure {
                LogUtils.e("onHandleWork-onFailure=${it.message}")
                if (isGrantedNotification()) {
                    BarNotificationCenter.updateNotice()
                }
            }
        }
    }

    companion object {
        private const val JOB_ID = 181821

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, NotificationJobIntentService::class.java, JOB_ID, work)
        }
    }

}