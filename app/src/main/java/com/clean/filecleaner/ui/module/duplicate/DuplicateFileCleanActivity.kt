package com.clean.filecleaner.ui.module.duplicate

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityDuplicateFileCleanBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DuplicateFileCleanActivity : BaseActivity<ActivityDuplicateFileCleanBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityDuplicateFileCleanBinding = ActivityDuplicateFileCleanBinding.inflate(layoutInflater)
    private var timeTag = 0L

    private fun setListeners() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@DuplicateFileCleanActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.ivRight.setOnClickListener {
            ToastUtils.showLong("todo")
        }

        binding.btnClean.setOnClickListener {
            CommonDialog(
                title = getString(R.string.warning),
                message = getString(R.string.do_you_wish_to_delete_this),
                leftBtn = getString(R.string.delete),
                rightBtn = getString(R.string.cancel),
                cancelable = true,
                leftClick = {
                    ToastUtils.showLong("delete")
                    // finish()
                }
            ).show(supportFragmentManager, "CommonDialog")
        }
    }


    override fun initView(savedInstanceState: Bundle?) {

        setListeners()

        with(binding) {
            toolbar.title.text = getString(R.string.duplicate_files)
            toolbar.ivRight.setImageResource(R.drawable.svg_duplicate_filter)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

        lifecycleScope.launch {
            delay(3000)
            TransitionManager.beginDelayedTransition(binding.root)
            binding.loadingView.isVisible = false
        }

    }

    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

}