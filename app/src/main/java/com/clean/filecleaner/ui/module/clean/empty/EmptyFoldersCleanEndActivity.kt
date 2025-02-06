package com.clean.filecleaner.ui.module.clean.empty

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
import com.clean.filecleaner.databinding.ActivityFileCleanEndBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.IAd
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.isBlocked
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.ad.showNativeAd
import com.clean.filecleaner.ui.ad.waitAdLoading
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.clean.duplicate.DuplicateFileCleanActivity
import com.clean.filecleaner.ui.module.clean.junk.JunkSearchActivity
import com.clean.filecleaner.ui.module.clean.screenshot.ScreenshotCleanActivity
import com.clean.filecleaner.ui.module.filemanager.FileCleanEndActivity.Companion.apk
import com.clean.filecleaner.ui.module.filemanager.FileCleanEndActivity.Companion.audio
import com.clean.filecleaner.ui.module.filemanager.FileCleanEndActivity.Companion.docs
import com.clean.filecleaner.ui.module.filemanager.FileCleanEndActivity.Companion.image
import com.clean.filecleaner.ui.module.filemanager.FileCleanEndActivity.Companion.video
import com.clean.filecleaner.ui.module.filemanager.MediaInfoParent
import com.clean.filecleaner.ui.module.filemanager.allFilesContainerList
import com.clean.filecleaner.ui.module.filemanager.allMediaList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EmptyFoldersCleanEndActivity : StoragePermissionBaseActivity<ActivityFileCleanEndBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityFileCleanEndBinding = ActivityFileCleanEndBinding.inflate(layoutInflater)

    private var startDeleteTime = 0L
    private val list by lazy { intent?.getStringArrayListExtra("EMPTY_FOLDERS") ?: emptyList() }

    private fun initBackListeners() {

        binding.loadingView.setOnClickListener { }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback {
            if (binding.loadingView.isVisible) {
                ToastUtils.showLong(getString(R.string.cleaning_back_tips))
            } else {
                startActivity(Intent(this@EmptyFoldersCleanEndActivity, MainActivity::class.java))
                finish()
            }
        }

        binding.clean.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@EmptyFoldersCleanEndActivity, JunkSearchActivity::class.java))
                finish()
            }
        }

        binding.screenshot.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@EmptyFoldersCleanEndActivity, ScreenshotCleanActivity::class.java))
                finish()
            }
        }

        binding.duplicateFiles.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@EmptyFoldersCleanEndActivity, DuplicateFileCleanActivity::class.java))
                finish()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.title.text = getString(R.string.delete)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.deleting)) {
                tvLoading.text = it
            }

            deleteFiles()

        }
        initBackListeners()
    }

    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    private fun deleteFiles() {
        startDeleteTime = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            var num = 0
            list.forEach { item ->
                val result = deleteItem(item)
                result.onSuccess {
                    num++
                }
            }
            val delayTime = startDeleteTime + 3000L - System.currentTimeMillis()
            if (delayTime > 0) {
                delay(delayTime)
            }
            withContext(Dispatchers.Main) {
                fullScreenAdShow {
                    TransitionManager.beginDelayedTransition(binding.root)
                    binding.loadingView.isVisible = false
                    binding.message.text = getString(R.string.folders_have_been_deleted, num)
                    stopLoadingAnim()
                    nativeAdShow()
                }
            }
        }
    }

    private fun deleteItem(path: String) = runCatching {
        val result = File(path).deleteRecursively()
        if (result) {
            MediaScannerConnection.scanFile(app, arrayOf(path), null) { _, _ -> }
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
                delay(210L)
            }
            adState.showFullScreenAd(this@EmptyFoldersCleanEndActivity, "fc_result_int") { onComplete() }
        }
    }

    private var ad: IAd? = null
    private fun nativeAdShow() {
        if (adManagerState.hasReachedUnusualAdLimit()) return
        DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_result_nat"))
        val adState = adManagerState.fcResultNatState
        adState.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(210L)
                if (adState.canShow()) {
                    ad?.destroy()
                    adState.showNativeAd(this@EmptyFoldersCleanEndActivity, binding.adContainer, "fc_result_nat") {
                        ad = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
        ad?.destroy()
        allFilesContainerList.clear()
        allMediaList.clear()
    }

}