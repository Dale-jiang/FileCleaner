package com.clean.filecleaner.ui.module.notification

import android.os.Parcelable
import androidx.annotation.Keep
import com.clean.filecleaner.R
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
sealed class BaseBarFunction(
    open val functionNameId: Int,
    open val functionIconId: Int
) : Parcelable

@Parcelize
@Keep
data object FuncClean : BaseBarFunction(
    functionNameId = R.string.clean,
    functionIconId = R.mipmap.ic_bar_notice_clean
)

@Parcelize
@Keep
data object FuncScreenShot : BaseBarFunction(
    functionNameId = R.string.screenshot,
    functionIconId = R.mipmap.ic_bar_notice_screenshot
)

@Parcelize
@Keep
data object FuncAntivirus : BaseBarFunction(
    functionNameId = R.string.antivirus,
    functionIconId = R.drawable.ic_bar_notice_antivirus
)

@Parcelize
@Keep
data class NotificationConfig(
    @SerializedName("fcon")
    val isOpen: Int,

    @SerializedName("fct")
    val timeInterval: Int,
    @SerializedName("fct_limit")
    val timeLimit: Int,

    @SerializedName("fcu")
    val unlockInterval: Int,
    @SerializedName("fcu_limit")
    val unlockLimit: Int,

    @SerializedName("fcunins")
    val uninstallInterval: Int,
    @SerializedName("fcuni_limit")
    val uninstallLimit: Int,

    @SerializedName("fcadd")
    val installInterval: Int,
    @SerializedName("fcadd_limit")
    val installLimit: Int,
) : Parcelable


@Parcelize
@Keep
sealed class BaseReminder(val reminderName: String) : Parcelable

@Parcelize
@Keep
data object TaskReminder : BaseReminder("time")

@Parcelize
@Keep
data object UserPresenceReminder : BaseReminder("unlock")

@Parcelize
@Keep
data object UninstallReminder : BaseReminder("unins")

@Parcelize
@Keep
data object InstallReminder : BaseReminder("ins")

@Parcelize
@Keep
data class NotificationInfo(
    val icon: Int,
    val messageId: Int,
    val btnStrId: Int,
    val function: BaseBarFunction,
    val reminder: BaseReminder,
    val notificationId: Int,
    var temp: String = ""
) : Parcelable


@Parcelize
@Keep
data class FloatingWindowConfig(
    @SerializedName("fcwindow_on")
    val isOpen: Int,

    @SerializedName("fcwindowt")
    val timeInterval: Int,
    @SerializedName("fcwindowt_limit")
    val timeLimit: Int,

    @SerializedName("fcwindowu")
    val unlockInterval: Int,
    @SerializedName("fcwindowu_limit")
    val unlockLimit: Int,

    @SerializedName("fcwindowunins")
    val uninstallInterval: Int,
    @SerializedName("fcwindowuni_limit")
    val uninstallLimit: Int,

    @SerializedName("fcwindowadd")
    val installInterval: Int,
    @SerializedName("fcwindowadd_limit")
    val installLimit: Int,
) : Parcelable