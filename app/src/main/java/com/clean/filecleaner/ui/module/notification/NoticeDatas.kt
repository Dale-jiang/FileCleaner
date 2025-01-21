package com.clean.filecleaner.ui.module.notification

import android.os.Parcelable
import com.clean.filecleaner.R
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class BaseBarFunction(
    open val functionNameId: Int,
    open val functionIconId: Int
) : Parcelable

@Parcelize
data object FuncClean : BaseBarFunction(
    functionNameId = R.string.clean,
    functionIconId = R.mipmap.ic_bar_notice_clean
)

@Parcelize
data object FuncScreenShot : BaseBarFunction(
    functionNameId = R.string.screenshot,
    functionIconId = R.mipmap.ic_bar_notice_screenshot
)