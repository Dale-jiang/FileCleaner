package com.clean.filecleaner.ui.module.junk.bean

import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import com.clean.filecleaner.R

@Keep
data class JunkSearchItem(val type: String, val icon: Int, var size: Long, var isLoading: Boolean, var time: Long = System.currentTimeMillis())


interface Selectable {
    var select: Boolean
}

interface FileSizeable {
    var fileSize: Long
}

interface Iconable {
    fun getImageIcon(): Int
}

interface Nameable {
    fun getName(): Int
}

enum class JunkType(val iconId: Int, val nameId: Int) {
    LOG_FILES(R.drawable.svg_log_files, R.string.log_files),
    TEMP_FILES(R.drawable.svg_temp_files, R.string.temp_files),
    APP_CACHE(R.drawable.svg_app_cache, R.string.app_cache),
    AD_JUNK(R.drawable.svg_ad_junk, R.string.ad_junk),
    APK_FILES(R.drawable.svg_apk_files, R.string.apk_files);

    fun getImageIcon(): Int = iconId
    fun getName(): Int = nameId
}

abstract class CleanJunkType : Selectable, FileSizeable, Iconable, Nameable {
    abstract val trashType: JunkType
    var isLoading: Boolean = false
    override fun getImageIcon(): Int = trashType.getImageIcon()
    override fun getName(): Int = trashType.getName()
}

data class TrashItem(val name: String, val path: String, override val trashType: JunkType, override var select: Boolean, override var fileSize: Long = 0L) : CleanJunkType()

data class TrashItemParent(
    val subItems: MutableList<CleanJunkType>,
    var isOpen: Boolean,
    override var fileSize: Long = 0L,
    override val trashType: JunkType,
    override var select: Boolean
) : CleanJunkType()

data class TrashItemCache(
    val name: String,
    val path: String = "",
    val pkgName: String,
    val drawable: Drawable? = null,
    override val trashType: JunkType = JunkType.APP_CACHE,
    override var select: Boolean = true,
    override var fileSize: Long = 0L
) : CleanJunkType()
