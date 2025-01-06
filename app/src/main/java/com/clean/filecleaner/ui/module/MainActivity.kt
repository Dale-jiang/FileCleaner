package com.clean.filecleaner.ui.module

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter.formatFileSize
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityMainBinding
import com.clean.filecleaner.ext.animateToProgressWithValueAnimator
import com.clean.filecleaner.ext.getStorageSizeInfo
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startScaleAnimation
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.app.ApplicationManagementActivity
import com.clean.filecleaner.ui.module.duplicate.DuplicateFileCleanActivity
import com.clean.filecleaner.ui.module.junk.JunkSearchActivity
import com.clean.filecleaner.ui.module.screenshot.ScreenshotCleanActivity

class MainActivity : StoragePermissionBaseActivity<ActivityMainBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()
        setStorageInfo()
    }

    override fun initView(savedInstanceState: Bundle?) {

        with(binding) {

            setStorageInfo()

            btnClean.startScaleAnimation()

            btnClean.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, JunkSearchActivity::class.java))
                }
            }

            btnSetting.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }

            appManager.setOnClickListener {
                startActivity(Intent(this@MainActivity, ApplicationManagementActivity::class.java))
            }

            screenshot.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, ScreenshotCleanActivity::class.java))
                }
            }

            duplicateFiles.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, DuplicateFileCleanActivity::class.java))
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setStorageInfo() {
        with(binding) {
            val storageInfo = getStorageSizeInfo()
            val usePercent = ((storageInfo.second.toFloat() / storageInfo.first) * 100).toInt()
            progressBar.animateToProgressWithValueAnimator(usePercent, 1000L) { value ->
                storagePercent.text = "${value}%"
            }
            storageInfoDes.text =
                getString(R.string.storage_of_used, formatFileSize(this@MainActivity, storageInfo.second), formatFileSize(this@MainActivity, storageInfo.first))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.btnClean.clearAnimation()
    }

}