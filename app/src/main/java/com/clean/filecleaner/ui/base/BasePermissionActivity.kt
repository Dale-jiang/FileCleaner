package com.clean.filecleaner.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.ext.hasAllStoragePermissions
import com.clean.filecleaner.ui.module.AutoCloseActivity
import com.clean.filecleaner.utils.AppLifeHelper.jumpToSettings

abstract class BasePermissionActivity<VB : ViewBinding> : BaseActivity<VB>() {

    protected var permissionRequestCallback: (Callback)? = null
    abstract fun requestPermissions(callback: Callback)

    private val settingsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
        handleLauncherResult()
    }

    protected val permissionsRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        handleLauncherResult()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    protected fun toAllFilesAccessSettings() {
        Intent(this, AutoCloseActivity::class.java).apply {
            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            settingsActivityLauncher.launch(this)
        }
    }

    protected fun toUsageAccessSettings() {
        Intent(this, AutoCloseActivity::class.java).apply {
            action = Settings.ACTION_USAGE_ACCESS_SETTINGS
            settingsActivityLauncher.launch(this)
        }
    }

    private fun handleLauncherResult() = run {
        jumpToSettings = false
        if (hasAllStoragePermissions()) {
            permissionRequestCallback?.invoke()
        }
        permissionRequestCallback = null
    }

    protected fun shouldShowPermissionRationale(permissions: Array<String>): Boolean {
        return permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
        }
    }

    protected fun toAppSettings() = runCatching {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        }.let { intent ->
            jumpToSettings = true
            settingsActivityLauncher.launch(intent)
        }
    }.onFailure {
        ToastUtils.showLong(getString(R.string.unknown_error))
    }

}