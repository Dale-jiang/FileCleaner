package com.clean.filecleaner.ui.module.junk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityJunkCleanEndBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class JunkCleanEndActivity : StoragePermissionBaseActivity<ActivityJunkCleanEndBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityJunkCleanEndBinding = ActivityJunkCleanEndBinding.inflate(layoutInflater)


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

            lifecycleScope.launch {
                delay(3000L)
                TransitionManager.beginDelayedTransition(root)
                loadingView.isVisible = false
                stopLoadingAnim()
            }

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

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }

}