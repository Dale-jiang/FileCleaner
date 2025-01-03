package com.clean.filecleaner.ext

import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


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