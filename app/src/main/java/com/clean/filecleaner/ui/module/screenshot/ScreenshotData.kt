package com.clean.filecleaner.ui.module.screenshot

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScreenshotCleanSub(
    val name: String = "",
    val size: Long = 0L,
    val time: Long = 0L,
    val path: String = "",
    var isSelected: Boolean = false
) {

    val timeStr
        get() = formatTime(time)

    private fun formatTime(time: Long): String {
        return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(time))
    }

    fun select() {
        isSelected = true
    }

    fun deselect() {
        isSelected = false
    }
}

data class ScreenshotCleanParent(
    val children: List<ScreenshotCleanSub> = emptyList(),
    var isSelected: Boolean = false,
    val time: Long = 0L,
) {

    val timeStr
        get() = formatTime(time)

    private fun formatTime(time: Long): String {
        return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(time))
    }

    fun select() {
        isSelected = true
    }

    fun deselect() {
        isSelected = false
    }
}