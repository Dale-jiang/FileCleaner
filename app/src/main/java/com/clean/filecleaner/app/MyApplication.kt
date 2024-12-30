package com.clean.filecleaner.app

import android.app.Application
import com.blankj.utilcode.util.ProcessUtils
import com.blankj.utilcode.util.SPStaticUtils
import com.blankj.utilcode.util.SPUtils
import com.clean.filecleaner.data.app

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        if (ProcessUtils.isMainProcess()) {
            SPStaticUtils.setDefaultSPUtils(SPUtils.getInstance("File_cleaner"))
        }
    }

}