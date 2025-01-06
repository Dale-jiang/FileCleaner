package com.clean.filecleaner.ui.module.clean.duplicate


data class DuplicateFileSub(
    val fileName: String = "",
    val filePath: String = "",
    val fileSize: Long = 0L,
    val fileSizeStr: String = "",
    val addTime: Long = 0L,
    val mimeType: String = "",
    var isLatest: Boolean = false,
    var isOldest: Boolean = false,
    var fileHash: String = "",
    var isSelected: Boolean = true
)


data class DuplicateFileGroup(
    val children: List<DuplicateFileSub> = emptyList()
)