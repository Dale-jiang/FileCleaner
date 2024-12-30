package com.clean.filecleaner.ui.module

import android.os.Bundle
import com.clean.filecleaner.databinding.ActivityMainBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override fun setupImmersiveMode() = immersiveMode()
    override fun inflateViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {



    }
}