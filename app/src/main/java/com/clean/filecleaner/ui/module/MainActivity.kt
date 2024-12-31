package com.clean.filecleaner.ui.module

import android.content.Intent
import android.os.Bundle
import com.clean.filecleaner.databinding.ActivityMainBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.junk.JunkSearchActivity

class MainActivity : StoragePermissionBaseActivity<ActivityMainBinding>() {
    override fun setupImmersiveMode() = immersiveMode()
    override fun inflateViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

        binding.request.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@MainActivity, JunkSearchActivity::class.java))
            }
        }

    }
}