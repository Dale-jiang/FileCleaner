package com.clean.filecleaner.ui.module

import android.os.Bundle
import com.clean.filecleaner.databinding.ActivityAppcacheTipsBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

class AppCacheTipsActivity : BaseActivity<ActivityAppcacheTipsBinding>() {
    override fun setupImmersiveMode() = immersiveMode(lightMode = false)
    override fun inflateViewBinding(): ActivityAppcacheTipsBinding = ActivityAppcacheTipsBinding.inflate(layoutInflater)
    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {
            root.setOnClickListener {
                finish()
                overridePendingTransition(0, 0)
            }
            btnIKnow.setOnClickListener {
                finish()
                overridePendingTransition(0, 0)
            }
        }
    }
}