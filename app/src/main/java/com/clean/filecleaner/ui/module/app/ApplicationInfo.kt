package com.clean.filecleaner.ui.module.app

import android.graphics.drawable.Drawable

data class ApplicationInfo(
    val drawable: Drawable? = null,
    val pkgName: String = "",
    val appName: String = "",
    var installTime: Long = 0L,
    var lastUsedTime: Long = 0L,
    var usedSize: Long = 0L
)