package com.clean.filecleaner.ui.module.clean.bigfiles

import androidx.annotation.Keep

@Keep
data class BigFileSelection(val name: String, var select: Boolean = false)

@Keep
enum class FileTypes {
    TYPE_APK, TYPE_AUDIO, TYPE_CERTIFICATE, TYPE_SOURCE_CODE,
    TYPE_ARCHIVES, TYPE_CONTACT, TYPE_EVENT, TYPE_FONT, TYPE_IMAGE,
    TYPE_PDF, TYPE_TEXT, TYPE_VIDEO, TYPE_WORD, TYPE_EXCEL, TYPE_PPT,
    TYPE_OTHER
}