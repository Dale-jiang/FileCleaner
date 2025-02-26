package com.clean.filecleaner.ui.module

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import com.clean.filecleaner.databinding.ActivityFloatingWindowPermissionBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.isOverlayPermissionGranted
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.base.FloatingPermissionBaseActivity

class FloatingWindowPermissionActivity : FloatingPermissionBaseActivity<ActivityFloatingWindowPermissionBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityFloatingWindowPermissionBinding = ActivityFloatingWindowPermissionBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {

            DataReportingUtils.postCustomEvent("winpop_page")

            onBackPressedDispatcher.addCallback {}

            btnSkip.setOnClickListener {
                startActivity(Intent(this@FloatingWindowPermissionActivity, MainActivity::class.java))
                finish()
            }

            btnContinue.setOnClickListener {
                DataReportingUtils.postCustomEvent("winpop_btn_click")
                requestPermissions {

                    if (isOverlayPermissionGranted()) {
                        DataReportingUtils.postCustomEvent("winpop_permiss_yes")
                    }

                    startActivity(Intent(this@FloatingWindowPermissionActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}


