package com.clean.filecleaner.app

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ProcessUtils
import com.clean.filecleaner.BuildConfig
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.initAF
import com.clean.filecleaner.ext.initRemoteConfig
import com.clean.filecleaner.ext.subscribeFCM
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter
import com.clean.filecleaner.ui.module.notification.notificationService
import com.clean.filecleaner.utils.AppLifeHelper
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.initialize

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        if (ProcessUtils.isMainProcess()) {
            AppLifeHelper.init(this)
            LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG)
            MobileAds.initialize(this)
            Firebase.initialize(this)
            Firebase.initRemoteConfig()
            BarNotificationCenter.init(this)
            DataReportingUtils.getAllInfos()
            notificationService.initialize()
            subscribeFCM()
            initAF()
        }
    }

}