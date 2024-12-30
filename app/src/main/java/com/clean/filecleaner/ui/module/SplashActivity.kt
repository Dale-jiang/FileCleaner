package com.clean.filecleaner.ui.module

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.SPStaticUtils
import com.clean.filecleaner.data.FIRST_LAUNCH
import com.clean.filecleaner.databinding.ActivitySplashBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun setupImmersiveMode() = immersiveMode()
    override fun inflateViewBinding(): ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

        startTimer(step = { interval, job ->
            if (interval > 20) {
                job?.cancel()
                SPStaticUtils.put(FIRST_LAUNCH, false)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }, end = {
            SPStaticUtils.put(FIRST_LAUNCH, false)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        })

    }
}