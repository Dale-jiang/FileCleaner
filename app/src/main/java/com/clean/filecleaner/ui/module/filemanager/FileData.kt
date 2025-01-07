package com.clean.filecleaner.ui.module.filemanager

import com.clean.filecleaner.ext.formatVideoDurationWithKotlinDuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


val allFilesContainerList = mutableListOf<FileInfo>()
val allMediaList = mutableListOf<MediaInfoParent>()

data class FileInfo(
    val name: String = "",
    val size: Long = 0L,
    val sizeString: String = "",
    val addTime: Long = 0L,
    val updateTime: Long = 0L,
    val duration: Long = 0L,
    val path: String = "",
    val mimetype: String = "",
    var isSelected: Boolean = false
) {
    val timeStr
        get() = formatTime(addTime)

    val durationStr
        get() = duration.formatVideoDurationWithKotlinDuration()

    private fun formatTime(time: Long): String {
        return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(time))
    }

}

data class MediaInfoParent(
    val children: List<FileInfo> = emptyList(),
    var isSelected: Boolean = false,
    val time: Long = 0L,
) {

    val timeStr
        get() = formatTime(time)

    private fun formatTime(time: Long): String {
        return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(time))
    }

}