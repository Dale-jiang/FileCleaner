package com.clean.filecleaner.ui.module.filemanager

data class FileInfo(
    val name: String = "",
    val size: Long = 0L,
    val sizeString: String = "",
    val addTime: Long = 0L,
    val updateTime: Long = 0L,
    val path: String = "",
    val mimetype: String = "",
    var isSelected: Boolean = false
)