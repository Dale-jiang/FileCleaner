package com.clean.filecleaner.ui.module.notification

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.blankj.utilcode.util.LogUtils

class BarService : Service() {

    private val tag = "BarService"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        updateNotice()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        updateNotice()
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    private fun updateNotice() {
        runCatching {
            this@BarService.startForeground(17777, BarNotificationCenter.updateNotice())
        }.onFailure {
            LogUtils.e(tag, it.message)
        }
    }

}