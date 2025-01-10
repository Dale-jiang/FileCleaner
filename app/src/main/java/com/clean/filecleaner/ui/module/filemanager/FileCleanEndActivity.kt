package com.clean.filecleaner.ui.module.filemanager

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
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.isBlocked
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.clean.duplicate.DuplicateFileCleanActivity
import com.clean.filecleaner.ui.module.clean.junk.JunkSearchActivity
import com.clean.filecleaner.ui.module.clean.screenshot.ScreenshotCleanActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileCleanEndActivity : StoragePermissionBaseActivity<ActivityFileCleanEndBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityFileCleanEndBinding = ActivityFileCleanEndBinding.inflate(layoutInflater)

    private var startDeleteTime = 0L

    companion object {
        const val docs = "docs"
        const val image = "image"
        const val video = "video"
        const val audio = "audio"
        const val apk = "apk"
    }

    private val type by lazy {
        intent?.getStringExtra("FILE_TYPES") ?: ""
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
                startActivity(Intent(this@FileCleanEndActivity, MainActivity::class.java))
                finish()
            }
        }

        binding.clean.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@FileCleanEndActivity, JunkSearchActivity::class.java))
                finish()
            }
        }

        binding.screenshot.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@FileCleanEndActivity, ScreenshotCleanActivity::class.java))
                finish()
            }
        }

        binding.duplicateFiles.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@FileCleanEndActivity, DuplicateFileCleanActivity::class.java))
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

            deleteFileByType()

        }
        initBackListeners()
    }

    private fun deleteFileByType() {

        when (type) {

            docs, apk, audio -> {
                deleteFiles(allFilesContainerList)
            }

            image, video -> {
                deleteMediaFiles(allMediaList)
            }

            else -> deleteFiles(emptyList<FileInfo>().toMutableList())

        }

    }

    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    private fun deleteMediaFiles(list: MutableList<MediaInfoParent>) {
        startDeleteTime = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            val itemsToDelete = list.toList()
                .flatMap { parent -> parent.children }
                .filter { it.isSelected }
            var num = 0
            itemsToDelete.forEach { item ->
                val result = deleteItem(item)
                result.onSuccess {
                    num++
                }
            }
            list.clear()
            val delayTime = startDeleteTime + 3000L - System.currentTimeMillis()
            if (delayTime > 0) {
                delay(delayTime)
            }
            withContext(Dispatchers.Main) {
                TransitionManager.beginDelayedTransition(binding.root)
                binding.loadingView.isVisible = false
                binding.message.text = getString(R.string.files_have_been_deleted, num)
                stopLoadingAnim()
            }
        }
    }

    private fun deleteFiles(files: MutableList<FileInfo>) {
        startDeleteTime = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            val itemsToDelete = files.filter { it.isSelected }
            var num = 0
            itemsToDelete.forEach { item ->
                val result = deleteItem(item)
                result.onSuccess {
                    num++
                }
            }
            files.clear()
            val delayTime = startDeleteTime + 3000L - System.currentTimeMillis()
            if (delayTime > 0) {
                delay(delayTime)
            }
            withContext(Dispatchers.Main) {
                fullScreenAdShow {
                    TransitionManager.beginDelayedTransition(binding.root)
                    binding.loadingView.isVisible = false
                    binding.message.text = getString(R.string.files_have_been_deleted, num)
                    stopLoadingAnim()
                }
            }
        }
    }

    private fun deleteItem(item: FileInfo) = runCatching {
        val result = File(item.path).deleteRecursively()
        if (result) {
            MediaScannerConnection.scanFile(app, arrayOf(item.path), arrayOf(item.mimetype)) { _, _ -> }
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
            adState.showFullScreenAd(this@FileCleanEndActivity, "fc_result_int") { onComplete() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
        allFilesContainerList.clear()
        allMediaList.clear()
    }

}