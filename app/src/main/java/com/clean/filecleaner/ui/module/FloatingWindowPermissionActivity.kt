package com.clean.filecleaner.ui.module

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import com.clean.filecleaner.databinding.ActivityFloatingWindowPermissionBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

class FloatingWindowPermissionActivity : BaseActivity<ActivityFloatingWindowPermissionBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityFloatingWindowPermissionBinding = ActivityFloatingWindowPermissionBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {

            onBackPressedDispatcher.addCallback {}

            binding.btnSkip.setOnClickListener {
                startActivity(Intent(this@FloatingWindowPermissionActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}


