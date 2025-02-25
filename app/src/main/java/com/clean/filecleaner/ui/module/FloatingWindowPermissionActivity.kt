package com.clean.filecleaner.ui.module

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.databinding.ActivityFloatingWindowPermissionBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.isOverlayPermissionGranted
import com.clean.filecleaner.ui.base.FloatingPermissionBaseActivity

class FloatingWindowPermissionActivity : FloatingPermissionBaseActivity<ActivityFloatingWindowPermissionBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityFloatingWindowPermissionBinding = ActivityFloatingWindowPermissionBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {

            onBackPressedDispatcher.addCallback {}

            btnSkip.setOnClickListener {
                startActivity(Intent(this@FloatingWindowPermissionActivity, MainActivity::class.java))
                finish()
            }

            btnContinue.setOnClickListener {
                requestPermissions {

                    if (isOverlayPermissionGranted()) {
                        ToastUtils.showLong("有权限")
                    } else {
                        ToastUtils.showLong("无权限")
                    }

                }
            }
        }
    }
}


