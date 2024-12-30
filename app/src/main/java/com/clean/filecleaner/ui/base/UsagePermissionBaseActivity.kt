package com.clean.filecleaner.ui.base

import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.SPStaticUtils
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.data.storagePermissions
import com.clean.filecleaner.ext.hasUsagePermissions
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid11OrAbove

abstract class UsagePermissionBaseActivity<VB : ViewBinding> : BasePermissionActivity<VB>() {

    override fun requestPermissions(callback: Callback) {
        permissionRequestCallback = callback

        if (hasUsagePermissions()) {
            callback.invoke()
            return
        }
        toUsageAccessSettings()
    }
}