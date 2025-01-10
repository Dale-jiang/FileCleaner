package com.clean.filecleaner.ui.module

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.clean.filecleaner.databinding.ActivitySplashBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.utils.AppPreferences.hasRequestUMP
import com.clean.filecleaner.utils.AppPreferences.isFirstLaunch
import com.clean.filecleaner.utils.UMPUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun setupImmersiveMode() = immersiveMode()
    override fun inflateViewBinding(): ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

        DataReportingUtils.postSessionEvent()
        DataReportingUtils.postCustomEvent("loading_page")
        if (!adManagerState.hasReachedUnusualAdLimit()){
            DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_launch"))
        }

        if (hasRequestUMP) {
            startLoading()
        } else {
            UMPUtils.requestUMPInfo(this) {
                hasRequestUMP = true
                startLoading()
            }
        }
    }

    private fun startLoading() {
        adManagerState.fcLaunchState.loadAd(this)
        startTimer(step = { interval, job ->
            if (adManagerState.fcLaunchState.canShow() && interval > 20) {

                job?.cancel()

                fullScreenAdShow {
                    isFirstLaunch = false
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }

            }
        }, end = {
            isFirstLaunch = false
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        })
    }


    private fun fullScreenAdShow(onComplete: () -> Unit) = runCatching {
        if (adManagerState.hasReachedUnusualAdLimit()) return@runCatching onComplete()
        val adState = adManagerState.fcLaunchState
        if (!adState.canShow()) {
            adState.loadAd(this)
            return@runCatching onComplete()
        }
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                delay(200L)
            }
            adState.showFullScreenAd(this@SplashActivity, "fc_launch", false) { onComplete() }
        }
    }

}

