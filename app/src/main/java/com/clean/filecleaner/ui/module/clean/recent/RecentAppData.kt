package com.clean.filecleaner.ui.module.clean.recent

import android.graphics.drawable.Drawable
import androidx.annotation.Keep

@Keep
data class AppLaunchInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val totalCount: Int,
    val foreground: Int,
    val background: Int,
    val temp: String = "",
)

@Keep
enum class LaunchType {
    TOTAL, FOREGROUND, BACKGROUND
}