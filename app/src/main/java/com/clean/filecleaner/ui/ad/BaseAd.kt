package com.clean.filecleaner.ui.ad

import android.content.Context
import android.view.ViewGroup
import com.clean.filecleaner.ui.base.BaseActivity

interface IAd {
    fun getAdLocation():String
    fun getAdConfigDetail(): AdConfigDetails?
    fun loadAd(context: Context, onLoaded: (success: Boolean, msg: String?) -> Unit = { _, _ -> })
    fun showAd(activity: BaseActivity<*>, parent: ViewGroup? = null, onClose: () -> Unit = {})
    fun isAdExpire(): Boolean
    fun destroy()
}