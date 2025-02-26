package com.clean.filecleaner.ui.module.antivirus

import android.os.Bundle
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityVirusScanBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.base.BaseActivity

class VirusScanActivity : BaseActivity<ActivityVirusScanBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityVirusScanBinding = ActivityVirusScanBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {

            toolbar.title.text = getString(R.string.antivirus)
            progress1.isIndeterminate = true
            progress2.isIndeterminate = true
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

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }
}