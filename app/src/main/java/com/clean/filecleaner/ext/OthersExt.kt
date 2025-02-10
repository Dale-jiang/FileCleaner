package com.clean.filecleaner.ext

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.documentfile.provider.DocumentFile
import com.clean.filecleaner.data.app
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration


fun DocumentFile.calculateDirectorySizeSafely(): Long {
    // 如果当前文件本身就是文件，直接返回其大小
    if (!isDirectory) return length()
    var totalSize = 0L
    val files = listFiles()
    files.forEach { file ->
        try {
            totalSize += if (file.isDirectory) {
                file.calculateDirectorySizeSafely()
            } else {
                file.length()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return totalSize
}


fun File.getFileSize(): Long {
    val queue = ArrayDeque<File>()
    queue.add(this)

    var totalSize = 0L
    while (queue.isNotEmpty()) {
        val current = queue.removeFirstOrNull() ?: continue
        if (!current.exists()) continue

        if (current.isDirectory) {
            current.listFiles()?.forEach { subFile ->
                queue.add(subFile)
            }
            totalSize += 4096
        } else {
            totalSize += current.length()
        }
    }
    return totalSize
}


fun Long.formatTimestampToMMddyyyy(): String {
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    val date = Date(this)
    return sdf.format(date)
}

fun String.getApkLogo(): Drawable? {
    return try {
        val packageManager = app.packageManager
        val packageInfo = packageManager.getPackageArchiveInfo(this, PackageManager.GET_ACTIVITIES)
        packageInfo?.applicationInfo?.let { appInfo ->
            appInfo.sourceDir = this
            appInfo.publicSourceDir = this
            packageManager.getApplicationIcon(appInfo)
        }
    } catch (e: Exception) {
        null
    }
}

fun Long.formatVideoDurationWithKotlinDuration(): String {
    val duration = this.toDuration(DurationUnit.MILLISECONDS)
    val totalSeconds = duration.inWholeSeconds
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours == 0L) {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}

fun Long.formatToDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}min ${seconds}s"
        minutes > 0 -> "${minutes}min ${seconds}s"
        else -> "${seconds}s"
    }
}

