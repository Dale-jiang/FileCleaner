package com.clean.filecleaner.app

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ProcessUtils
import com.blankj.utilcode.util.SPStaticUtils
import com.blankj.utilcode.util.SPUtils
import com.clean.filecleaner.BuildConfig
import com.clean.filecleaner.data.app
import com.clean.filecleaner.utils.AppLifeHelper

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        if (ProcessUtils.isMainProcess()) {
            AppLifeHelper.init(this)
            LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG)
            SPStaticUtils.setDefaultSPUtils(SPUtils.getInstance("File_cleaner"))
        }
    }

}