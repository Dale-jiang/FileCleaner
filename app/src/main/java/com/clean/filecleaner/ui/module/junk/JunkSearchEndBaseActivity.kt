package com.clean.filecleaner.ui.module.junk

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.SPStaticUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.data.app
import com.clean.filecleaner.data.storagePermissions
import com.clean.filecleaner.ext.hasUsagePermissions
import com.clean.filecleaner.ui.base.UsagePermissionBaseActivity
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid11OrAbove
import com.clean.filecleaner.utils.AppLifeHelper.isFromSettings

abstract class JunkSearchEndBaseActivity<VB : ViewBinding> : UsagePermissionBaseActivity<VB>() {

    abstract fun folderLauncherEnd()
    abstract fun above12LauncherEnd()


    protected val folderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        runCatching {
            isFromSettings = false
            val folderUri = it.data?.data
            if (null != folderUri && it.resultCode == RESULT_OK) {
                app.contentResolver.takePersistableUriPermission(
                    folderUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                folderLauncherEnd()
            }
        }
    }

    protected val above12Launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            above12LauncherEnd()
        }
    }
}