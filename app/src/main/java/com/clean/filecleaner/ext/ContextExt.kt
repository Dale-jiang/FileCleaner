package com.clean.filecleaner.ext

import android.app.Application
import android.content.Context
import android.os.PowerManager

fun Application.canInteractive() = runCatching { (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive }.getOrNull() ?: false