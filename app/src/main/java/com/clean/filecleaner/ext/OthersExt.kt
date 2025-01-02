package com.clean.filecleaner.ext

import androidx.documentfile.provider.DocumentFile
import java.io.File


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
            // 根据业务需求处理异常（记录日志、忽略、提示用户等）
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
            // 这里加上 4096 用来统计目录本身所占用的一些空间（对 ext4 等文件系统来说可能近似）
            totalSize += 4096
        } else {
            totalSize += current.length()
        }
    }
    return totalSize
}
