package com.clean.filecleaner.ui.module

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter.formatFileSize
import android.view.View
import androidx.core.animation.doOnEnd
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityMainBinding
import com.clean.filecleaner.ext.animateToProgressWithValueAnimator
import com.clean.filecleaner.ext.getStorageSizeInfo
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.ui.module.junk.JunkSearchActivity

class MainActivity : StoragePermissionBaseActivity<ActivityMainBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun onResume() {
        super.onResume()
        setStorageInfo()
    }

    override fun initView(savedInstanceState: Bundle?) {

        with(binding) {

            setStorageInfo()
            startHeartbeatAnimation(btnClean)

            btnClean.setOnClickListener {
                requestPermissions {
                    startActivity(Intent(this@MainActivity, JunkSearchActivity::class.java))
                }
            }

            btnSetting.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }

            appManager.setOnClickListener {
                CommonDialog(message = "dsfsdfsdfsd", leftBtn = "Cancel", rightBtn = "OK", cancelable = true,
                    leftClick = {

                    },
                    rightClick = {

                    }).show(supportFragmentManager, "CommonDialog")

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setStorageInfo() {
        with(binding) {
            val storageInfo = getStorageSizeInfo()
            val usePercent = ((storageInfo.second.toFloat() / storageInfo.first) * 100).toInt()
            progressBar.animateToProgressWithValueAnimator(usePercent, 1000L) { value ->
                storagePercent.text = "${value}%"
            }
            storageInfoDes.text =
                getString(R.string.storage_of_used, formatFileSize(this@MainActivity, storageInfo.second), formatFileSize(this@MainActivity, storageInfo.first))
        }
    }

    private fun startHeartbeatAnimation(view: View) {
        val scaleXUp = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f, 1.1f, 1f).apply { duration = 600 }
        val scaleYUp = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f, 1.1f, 1f).apply { duration = 600 }
        val animatorSet = AnimatorSet().apply {
            playTogether(scaleXUp, scaleYUp)
            doOnEnd {
                view.postDelayed({ startHeartbeatAnimation(view) }, 2500L)
            }
        }
        animatorSet.start()
    }
}