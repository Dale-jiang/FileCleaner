package com.clean.filecleaner.utils

import android.content.Context
import android.content.SharedPreferences
import com.clean.filecleaner.data.app

object AppPreferences {

    val prefs: SharedPreferences by lazy {
        app.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    }

    var isFirstLaunch by prefs.prefDelegate(true)
    var lastCleanCacheTime by prefs.prefDelegate(0L)
    var appInstallTime by prefs.prefDelegate(0L)
    var unusualAdClickCount by prefs.prefDelegate(0)
    var unusualAdShowCount by prefs.prefDelegate(0)
    var isUnusualUser by prefs.prefDelegate(false)

    var distinctId by prefs.prefDelegate("")

}