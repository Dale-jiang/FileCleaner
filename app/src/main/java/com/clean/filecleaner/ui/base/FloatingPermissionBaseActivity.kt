package com.clean.filecleaner.ui.base

import androidx.viewbinding.ViewBinding
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.ext.isOverlayPermissionGranted

abstract class FloatingPermissionBaseActivity<VB : ViewBinding> : BasePermissionActivity<VB>() {

    override fun requestPermissions(callback: Callback) {
        permissionRequestCallback = callback

        if (isOverlayPermissionGranted()) {
            callback.invoke()
            return
        }
        toFloatingAccessSettings()
    }
}