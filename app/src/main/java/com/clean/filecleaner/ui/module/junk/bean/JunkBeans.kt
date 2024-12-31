package com.clean.filecleaner.ui.module.junk.bean

import androidx.annotation.Keep

@Keep
data class JunkSearchItem(val type: String, val icon: Int, var size: Long, var isLoading: Boolean, var time: Long = System.currentTimeMillis())