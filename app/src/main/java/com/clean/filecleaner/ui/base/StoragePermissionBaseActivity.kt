package com.clean.filecleaner.ui.base

import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.SPStaticUtils
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.data.storagePermissions
import com.clean.filecleaner.ext.hasAllStoragePermissions
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid11OrAbove

abstract class StoragePermissionBaseActivity<VB : ViewBinding> : BasePermissionActivity<VB>() {

    override fun requestPermissions(callback: Callback) {
        permissionRequestCallback = callback

        if (hasAllStoragePermissions()) {
            callback.invoke()
            return
        }

        if (isAndroid11OrAbove()) {
            toAllFilesAccessSettings()
        } else {
            handleBelowRPermissions()
        }
    }


    private fun handleBelowRPermissions() {
        val permissions = storagePermissions
        if (!SPStaticUtils.getBoolean("hasRequestedPermissions", false)) {
            SPStaticUtils.put("hasRequestedPermissions", true)
            permissionsRequestLauncher.launch(permissions)
        } else {
            if (shouldShowPermissionRationale(permissions)) {
                permissionsRequestLauncher.launch(permissions)
            } else {
                toAppSettings()
            }
        }
    }

}