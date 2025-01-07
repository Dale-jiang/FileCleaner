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
import com.clean.filecleaner.ui.module.clean.app.ApplicationManagementActivity
import com.clean.filecleaner.ui.module.clean.duplicate.DuplicateFileCleanActivity
import com.clean.filecleaner.ui.module.clean.junk.JunkSearchActivity
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.allJunkDataList
import com.clean.filecleaner.ui.module.clean.screenshot.ScreenshotCleanActivity
import com.clean.filecleaner.ui.module.filemanager.allFilesContainerList
import com.clean.filecleaner.ui.module.filemanager.allMediaList
import com.clean.filecleaner.ui.module.filemanager.apk.ManageAPKActivity
import com.clean.filecleaner.ui.module.filemanager.audio.ManageAudioActivity
import com.clean.filecleaner.ui.module.filemanager.docs.ManageDocsActivity
import com.clean.filecleaner.ui.module.filemanager.image.ManageImageActivity
import com.clean.filecleaner.ui.module.filemanager.video.ManageVideoActivity

class MainActivity : StoragePermissionBaseActivity<ActivityMainBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()
        setStorageInfo()
        resetGlobalList()
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


            docs.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, ManageDocsActivity::class.java))
                }
            }

            apk.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, ManageAPKActivity::class.java))
                }
            }

            audio.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, ManageAudioActivity::class.java))
                }
            }

            image.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, ManageImageActivity::class.java))
                }
            }

            video.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, ManageVideoActivity::class.java))
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
        resetGlobalList()
    }

    private fun resetGlobalList() {
        runCatching {
            allJunkDataList.clear()
            ScreenshotCleanActivity.allScreenshotList.clear()
            DuplicateFileCleanActivity.allDuplicateFileList.clear()
            allFilesContainerList.clear()
            allMediaList.clear()
        }
    }

}