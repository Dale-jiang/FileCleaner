package com.clean.filecleaner.ui.module.clean.screenshot

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import com.clean.filecleaner.databinding.ActivityScreenshotCleanEndBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.isBlocked
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.clean.app.ApplicationManagementActivity
import com.clean.filecleaner.ui.module.clean.duplicate.DuplicateFileCleanActivity
import com.clean.filecleaner.ui.module.clean.junk.JunkSearchActivity
import com.clean.filecleaner.ui.module.clean.screenshot.ScreenshotCleanActivity.Companion.allScreenshotList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ScreenshotCleanEndActivity : StoragePermissionBaseActivity<ActivityScreenshotCleanEndBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityScreenshotCleanEndBinding = ActivityScreenshotCleanEndBinding.inflate(layoutInflater)

    private var startDeleteTime = 0L

    private val cleanSize by lazy {
        intent?.getStringExtra("MESSAGE") ?: ""
    }

    private fun initBackListeners() {

        binding.loadingView.setOnClickListener { }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback {
            if (binding.loadingView.isVisible) {
                ToastUtils.showLong(getString(R.string.cleaning_back_tips))
            } else {
                startActivity(Intent(this@ScreenshotCleanEndActivity, MainActivity::class.java))
                finish()
            }
        }

        binding.clean.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@ScreenshotCleanEndActivity, JunkSearchActivity::class.java))
                finish()
            }
        }

        binding.appManager.setOnClickListener {
            startActivity(Intent(this@ScreenshotCleanEndActivity, ApplicationManagementActivity::class.java))
            finish()
        }

        binding.duplicateFiles.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@ScreenshotCleanEndActivity, DuplicateFileCleanActivity::class.java))
                finish()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.title.text = getString(R.string.screenshot)
            message.text = cleanSize
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.cleaning)) {
                tvLoading.text = it
            }

            deleteScreenshot()

        }
        initBackListeners()
    }

    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    private fun deleteScreenshot() {
        startDeleteTime = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            val itemsToDelete = allScreenshotList.toList()
                .flatMap { parent -> parent.children }
                .filter { it.isSelected }

            itemsToDelete.forEach { item -> deleteItem(item) }
            allScreenshotList.clear()
            val delayTime = startDeleteTime + 3000L - System.currentTimeMillis()
            if (delayTime > 0) {
                delay(delayTime)
            }
            withContext(Dispatchers.Main) {
                fullScreenAdShow {
                    TransitionManager.beginDelayedTransition(binding.root)
                    binding.loadingView.isVisible = false
                    stopLoadingAnim()
                }
            }
        }
    }

    private fun deleteItem(item: ScreenshotCleanSub) = runCatching {
        val result = File(item.path).deleteRecursively()
        if (result) {
            MediaScannerConnection.scanFile(app, arrayOf(item.path), null) { _, _ -> }
        }
    }.onFailure {
        LogUtils.e(it.message)
    }

    private fun fullScreenAdShow(onComplete: () -> Unit) = runCatching {
        if (adManagerState.isBlocked() || adManagerState.hasReachedUnusualAdLimit()) return@runCatching onComplete()
        DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_result_int"))
        val adState = adManagerState.fcResultIntState
        if (!adState.canShow()) {
            adState.loadAd(this)
            return@runCatching onComplete()
        }
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                delay(200L)
            }
            adState.showFullScreenAd(this@ScreenshotCleanEndActivity, "fc_result_int") { onComplete() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
        allScreenshotList.clear()
    }

}