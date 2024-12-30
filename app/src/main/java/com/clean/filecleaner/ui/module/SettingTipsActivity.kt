package com.clean.filecleaner.ui.module

import android.os.Bundle
import com.clean.filecleaner.databinding.ActivitySettingTipsBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

class SettingTipsActivity : BaseActivity<ActivitySettingTipsBinding>() {
    override fun setupImmersiveMode() = immersiveMode(lightMode = false)
    override fun inflateViewBinding(): ActivitySettingTipsBinding = ActivitySettingTipsBinding.inflate(layoutInflater)
    private val tips by lazy { intent?.getStringExtra("SETTING_MESSAGE") }
    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {
            tips?.run { des.text = tips ?: "" }
            root.setOnClickListener { finish() }
            btnGot.setOnClickListener { finish() }
        }
    }
}