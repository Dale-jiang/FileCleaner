package com.clean.filecleaner.ui.module.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityApplicationManagementBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity

class ApplicationManagementActivity : BaseActivity<ActivityApplicationManagementBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityApplicationManagementBinding = ActivityApplicationManagementBinding.inflate(layoutInflater)

    private fun setBackListener() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@ApplicationManagementActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        setBackListener()

        with(binding) {
            toolbar.title.text = getString(R.string.app_manager)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

    }


    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

}