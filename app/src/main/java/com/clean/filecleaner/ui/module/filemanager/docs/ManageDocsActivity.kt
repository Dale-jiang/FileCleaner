package com.clean.filecleaner.ui.module.filemanager.docs

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityManageDocsBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ManageDocsActivity : BaseActivity<ActivityManageDocsBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityManageDocsBinding = ActivityManageDocsBinding.inflate(layoutInflater)

    private var adapter: ManageDocsAdapter? = null

    private fun setListeners() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@ManageDocsActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        setListeners()

        with(binding) {
            toolbar.title.text = getString(R.string.docs)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

        lifecycleScope.launch {
            delay(3000L)
            stopLoadingAnim()
            TransitionManager.beginDelayedTransition(binding.root)
            binding.loadingView.isVisible = false
            binding.bottomView.isVisible = true
            setUpAdapter()
        }
    }

    private fun setUpAdapter() {
        with(binding) {
            adapter = ManageDocsAdapter(this@ManageDocsActivity, list = mutableListOf()) {

            }
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@ManageDocsActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }


    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }
}