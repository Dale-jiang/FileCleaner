package com.clean.filecleaner.ui.module.clean.junk

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import com.clean.filecleaner.databinding.ActivityJunkCleanEndBinding
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
import com.clean.filecleaner.ui.module.clean.app.ApplicationManagementActivity
import com.clean.filecleaner.ui.module.clean.duplicate.DuplicateFileCleanActivity
import com.clean.filecleaner.ui.module.clean.junk.bean.CleanJunkType
import com.clean.filecleaner.ui.module.clean.junk.bean.TrashItem
import com.clean.filecleaner.ui.module.clean.junk.bean.TrashItemCache
import com.clean.filecleaner.ui.module.clean.junk.bean.TrashItemParent
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.allJunkDataList
import com.clean.filecleaner.ui.module.clean.screenshot.ScreenshotCleanActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class JunkCleanEndActivity : StoragePermissionBaseActivity<ActivityJunkCleanEndBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityJunkCleanEndBinding = ActivityJunkCleanEndBinding.inflate(layoutInflater)

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
                startActivity(Intent(this@JunkCleanEndActivity, MainActivity::class.java))
                finish()
            }
        }

        binding.duplicateFiles.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@JunkCleanEndActivity, DuplicateFileCleanActivity::class.java))
                finish()
            }
        }

        binding.appManager.setOnClickListener {
            startActivity(Intent(this@JunkCleanEndActivity, ApplicationManagementActivity::class.java))
            finish()
        }

        binding.screenshot.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@JunkCleanEndActivity, ScreenshotCleanActivity::class.java))
                finish()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.title.text = getString(R.string.clean)
            if (cleanSize.isEmpty()) {
                messageTitle.text = getString(R.string.no_junk_found)
                binding.loadingView.isVisible = false
            } else {
                message.text = cleanSize
                messageTitle.text = getString(R.string.clean_finished)
                ivLoading.startRotatingWithRotateAnimation()
                showLoadingAnimation(preStr = getString(R.string.cleaning)) {
                    tvLoading.text = it
                }

                deleteJunk()
            }

        }
        initBackListeners()
    }

    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    private fun deleteJunk() {
        startDeleteTime = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            val itemsToDelete = allJunkDataList
                .filterIsInstance<TrashItemParent>()
                .flatMap { parent -> parent.subItems }
                .filter { it.select }

            itemsToDelete.forEach { item -> deleteItem(item) }
            allJunkDataList.clear()
            val delayTime = startDeleteTime + 3000L - System.currentTimeMillis()
            if (delayTime > 0) {
                delay(delayTime)
            }
            withContext(Dispatchers.Main) {
                fullScreenAdShow{
                    TransitionManager.beginDelayedTransition(binding.root)
                    binding.loadingView.isVisible = false
                    stopLoadingAnim()
                    nativeAdShow()
                }
            }
        }
    }

    private fun deleteItem(item: CleanJunkType) = runCatching {
        when (item) {
            is TrashItemCache -> {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                    DocumentFile.fromSingleUri(app, Uri.parse(item.path))
                        ?.takeIf { it.exists() }
                        ?.delete()
                } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    File(item.path).deleteRecursively()
                } else Unit
            }

            is TrashItem -> {
                File(item.path).deleteRecursively()
            }

            else -> Unit
        }
    }.onFailure {
        LogUtils.e(it.message)
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
                    adState.showNativeAd(this@JunkCleanEndActivity, binding.adContainer, "fc_result_nat") {
                        ad = it
                    }
                }
            }
        }
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
            adState.showFullScreenAd(this@JunkCleanEndActivity, "fc_result_int") { onComplete() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
        allJunkDataList.clear()
    }

}