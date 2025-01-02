package com.clean.filecleaner.ui.module.junk

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
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
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.junk.bean.CleanJunkType
import com.clean.filecleaner.ui.module.junk.bean.TrashItem
import com.clean.filecleaner.ui.module.junk.bean.TrashItemCache
import com.clean.filecleaner.ui.module.junk.bean.TrashItemParent
import com.clean.filecleaner.ui.module.junk.viewmodel.allJunkDataList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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

    private var loadingJob: Job? = null

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

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.title.text = getString(R.string.clean)
            message.text = cleanSize
            ivLoading.startRotatingWithRotateAnimation()
            loadingJob = showLoadingAnimation()

            deleteJunk()

        }
        initBackListeners()
    }

    private fun showLoadingAnimation(delayTime: Long = 500): Job {
        return lifecycleScope.launch(Dispatchers.Main) {
            var dotCount = 0
            while (isActive) {
                val str = getString(R.string.loading) + ".".repeat(dotCount + 1)
                binding.tvLoading.text = str
                dotCount = (dotCount + 1) % 3
                delay(delayTime)
            }
        }
    }

    private fun stopLoadingAnim() {
        loadingJob?.cancel()
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
                TransitionManager.beginDelayedTransition(binding.root)
                binding.loadingView.isVisible = false
                stopLoadingAnim()
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


    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }

}