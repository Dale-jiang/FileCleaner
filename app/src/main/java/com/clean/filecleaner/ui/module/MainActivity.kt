package com.clean.filecleaner.ui.module

import android.os.Bundle
import com.clean.filecleaner.databinding.ActivityMainBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.dialog.CommonDialog

class MainActivity : StoragePermissionBaseActivity<ActivityMainBinding>() {
    override fun setupImmersiveMode() = immersiveMode()
    override fun inflateViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

//        TransitionManager.beginDelayedTransition(binding.root)

        binding.request.setOnClickListener {
            requestPermissions {
                //  startActivity(Intent(this@MainActivity, JunkSearchActivity::class.java))


                CommonDialog(message = "dsfsdfsdfsd", leftBtn = "Cancel", rightBtn = "OK", cancelable = true,
                    leftClick = {

                    },
                    rightClick = {

                    }).show(supportFragmentManager, "CommonDialog")


            }
        }

    }
}