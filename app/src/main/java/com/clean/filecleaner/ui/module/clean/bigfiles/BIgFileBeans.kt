package com.clean.filecleaner.ui.module.clean.bigfiles

import androidx.annotation.Keep

@Keep
data class BigFileSelection(val name: String, var select: Boolean = false)