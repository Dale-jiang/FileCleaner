package com.clean.filecleaner.ui.module.antivirus

import android.graphics.drawable.Drawable


val allVirusList = mutableListOf<VirusInfo>()

data class VirusInfo(
    var label: String = "",
    var path: String = "",
    var score: Int = 0,
    val packageName: String = "",
    val virusType: String = "",
    val isApp: Boolean = false,
    val drawable: Drawable? = null,
    var temp: String = ""
)