package com.clean.filecleaner.ui.module

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityAutoCloseBinding
import com.clean.filecleaner.ext.hasAllStoragePermissions
import com.clean.filecleaner.ext.hasUsagePermissions
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid10OrAbove
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoCloseActivity : BaseActivity<ActivityAutoCloseBinding>() {

    override fun setupImmersiveMode() = immersiveMode()
    override fun inflateViewBinding(): ActivityAutoCloseBinding = ActivityAutoCloseBinding.inflate(layoutInflater)

    private var permissionCheckJob: Job? = null
    private var permissionHasRequested: Boolean = false

    companion object {
        private const val TAG = "AutoCloseActivity"
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.root.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        runCatching {
            val action = intent.action
            if (permissionHasRequested || action.isNullOrBlank()) {
                cancelPermissionCheck()
                finish()
            } else {
                permissionHasRequested = true
                requestPermission(action)
                monitorPermissionStatus()
            }
        }

    }

    @SuppressLint("InlinedApi")
    private fun requestPermission(action: String) {
        try {
            val tipString = getString(R.string.make_sure_this_option_is_enabled)
            when (action) {
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Settings.ACTION_USAGE_ACCESS_SETTINGS -> {
                    createAndLaunchSettingsIntent(action, tipString)
                }

                else -> {
                    LogUtils.w(TAG, "Unsupported action: $action")
                }
            }
        } catch (e: ActivityNotFoundException) {
            LogUtils.e(TAG, "Failed to launch settings for action: $action", e)
        } catch (e: Exception) {
            LogUtils.e(TAG, "Unexpected error while requesting permission", e)
        }
    }

    private fun createAndLaunchSettingsIntent(action: String, tipString: String): Intent {
        val intent = Intent(action).apply {
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            if (action == Settings.ACTION_USAGE_ACCESS_SETTINGS && isAndroid10OrAbove()) {
                data = Uri.parse("package:$packageName")
            } else if (action == Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION) {
                data = Uri.parse("package:$packageName")
            }
        }

        runCatching {
            startActivity(intent)
        }.onFailure {
            if (action == Settings.ACTION_USAGE_ACCESS_SETTINGS) {
                startActivity(Intent(action).apply {
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                })
            }
        }

        lifecycleScope.launch {
            delay(300)
            startActivity(Intent(this@AutoCloseActivity, SettingTipsActivity::class.java).apply {
                putExtra("SETTING_MESSAGE", tipString)
            })
        }
        return intent
    }

    private fun monitorPermissionStatus() = runCatching {
        cancelPermissionCheck()
        permissionCheckJob = lifecycleScope.launch {
            while (!isPermissionGranted()) {
                delay(200)
            }
            if (!isFinishing) {
                restartActivity()
            }
        }
    }

    private fun restartActivity() {
        startActivity(Intent(this, AutoCloseActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    private fun cancelPermissionCheck() {
        permissionCheckJob?.cancel()
        permissionCheckJob = null
    }

    private fun isPermissionGranted(): Boolean {
        return when (intent.action) {
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION -> hasAllStoragePermissions()
            Settings.ACTION_USAGE_ACCESS_SETTINGS -> hasUsagePermissions()
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelPermissionCheck()
    }

}