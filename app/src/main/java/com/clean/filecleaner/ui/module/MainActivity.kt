package com.clean.filecleaner.ui.module

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.format.Formatter.formatFileSize
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import com.clean.filecleaner.databinding.ActivityMainBinding
import com.clean.filecleaner.ext.animateToProgressWithValueAnimator
import com.clean.filecleaner.ext.getStorageSizeInfo
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.isGrantedNotification
import com.clean.filecleaner.ext.startScaleAnimation
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.IAd
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.canShowBackAd
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.isBlocked
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.ad.showNativeAd
import com.clean.filecleaner.ui.ad.waitAdLoading
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.clean.app.ApplicationManagementActivity
import com.clean.filecleaner.ui.module.clean.duplicate.DuplicateFileCleanActivity
import com.clean.filecleaner.ui.module.clean.junk.JunkSearchActivity
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.allJunkDataList
import com.clean.filecleaner.ui.module.clean.screenshot.ScreenshotCleanActivity
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.ui.module.filemanager.allFilesContainerList
import com.clean.filecleaner.ui.module.filemanager.allMediaList
import com.clean.filecleaner.ui.module.filemanager.apk.ManageAPKActivity
import com.clean.filecleaner.ui.module.filemanager.audio.ManageAudioActivity
import com.clean.filecleaner.ui.module.filemanager.docs.ManageDocsActivity
import com.clean.filecleaner.ui.module.filemanager.image.ManageImageActivity
import com.clean.filecleaner.ui.module.filemanager.video.ManageVideoActivity
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter.FUNCTION_TYPE
import com.clean.filecleaner.ui.module.notification.BaseBarFunction
import com.clean.filecleaner.ui.module.notification.FuncClean
import com.clean.filecleaner.ui.module.notification.FuncScreenShot
import com.clean.filecleaner.utils.AndroidVersionUtils
import com.clean.filecleaner.utils.AppLifeHelper.jumpToSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : StoragePermissionBaseActivity<ActivityMainBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
    private val barFunction by lazy { intent?.getParcelableExtra<BaseBarFunction>(FUNCTION_TYPE) }
    private var hasRequestedNotification = false
    private val notificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (isGrantedNotification()){
                DataReportingUtils.postCustomEvent("PermYes")
            }else{
                DataReportingUtils.postCustomEvent("PermNo")
            }
        }
    private val notificationSetLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isGrantedNotification()){
                DataReportingUtils.postCustomEvent("PermYes")
            }else{
                DataReportingUtils.postCustomEvent("PermNo")
            }
            jumpToSettings = false
        }

    override fun onResume() {
        super.onResume()
        if (canShowBackAd) {
            canShowBackAd = false
            fullScreenAdShow {
                setStorageInfo()
                resetGlobalList()
                nativeAdShow()
            }
        } else nativeAdShow()
    }

    override fun initView(savedInstanceState: Bundle?) {

        with(binding) {

            adManagerState.fcBackScanIntState.loadAd(this@MainActivity)
            adManagerState.fcScanNatState.loadAd(this@MainActivity)

            setStorageInfo()
            btnClean.startScaleAnimation()
            checkNotification()

            btnClean.setOnClickListener {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, JunkSearchActivity::class.java))
                }
            }

            btnSetting.setOnClickListener {
                canShowBackAd = true
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }

            appManager.setOnClickListener {
                canShowBackAd = true
                startActivity(Intent(this@MainActivity, ApplicationManagementActivity::class.java))
            }

            screenshot.setOnClickListener {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, ScreenshotCleanActivity::class.java))
                }
            }

            duplicateFiles.setOnClickListener {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, DuplicateFileCleanActivity::class.java))
                }
            }


            docs.setOnClickListener {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, ManageDocsActivity::class.java))
                }
            }

            apk.setOnClickListener {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, ManageAPKActivity::class.java))
                }
            }

            audio.setOnClickListener {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, ManageAudioActivity::class.java))
                }
            }

            image.setOnClickListener {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, ManageImageActivity::class.java))
                }
            }

            video.setOnClickListener {
                requestPermissions {
                    canShowBackAd = true
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

    private var ad: IAd? = null
    private fun nativeAdShow() {
        if (adManagerState.hasReachedUnusualAdLimit()) return
        DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_main_nat"))
        val adState = adManagerState.fcMainNatState
        adState.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(210L)
                if (adState.canShow()) {
                    ad?.destroy()
                    adState.showNativeAd(this@MainActivity, binding.adContainer, "fc_main_nat") {
                        ad = it
                    }
                }
            }
        }
    }

    private fun checkNotification() {
        when (barFunction) {
            FuncScreenShot -> {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, ScreenshotCleanActivity::class.java))
                }
            }

            FuncClean -> {
                requestPermissions {
                    canShowBackAd = true
                    startActivity(Intent(this@MainActivity, JunkSearchActivity::class.java))
                }
            }

            else -> requestNotification()
        }
    }

    private fun requestNotification() = runCatching {
        if (isGrantedNotification()) return@runCatching
        if (hasRequestedNotification) return@runCatching
        hasRequestedNotification = true
        if (AndroidVersionUtils.isAndroid13OrAbove() && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            CommonDialog(
                title = getString(R.string.confirm),
                message = getString(R.string.turn_on_notifications),
                rightBtn = getString(R.string.grant),
                rightClick = {
                    jumpToSettings = true
                    notificationSetLauncher.launch(getNoticeSetPageIntent())
                },
                leftBtn = getString(R.string.cancel), leftClick = {}).show(supportFragmentManager, "requestNotification")
        }
    }


    private fun getNoticeSetPageIntent(): Intent {
        return if (AndroidVersionUtils.isAndroid8OrAbove()) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, app.packageName) }
        } else Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
            putExtra("app_package", this@MainActivity.packageName)
            putExtra("app_uid", this@MainActivity.applicationInfo.uid)
        }
    }

    private fun fullScreenAdShow(onComplete: () -> Unit) = runCatching {
        if (adManagerState.isBlocked() || adManagerState.hasReachedUnusualAdLimit()) return@runCatching onComplete()
        DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_back_int"))
        val adState = adManagerState.fcBackScanIntState
        if (!adState.canShow()) {
            adState.loadAd(this)
            return@runCatching onComplete()
        }
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                delay(210L)
            }
            adState.showFullScreenAd(this@MainActivity, "fc_back_int") { onComplete() }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.btnClean.clearAnimation()
        ad?.destroy()
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