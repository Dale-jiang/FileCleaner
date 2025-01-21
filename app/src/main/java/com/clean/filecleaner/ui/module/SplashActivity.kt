package com.clean.filecleaner.ui.module

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.clean.filecleaner.databinding.ActivitySplashBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.isGrantedNotification
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter.FUNCTION_TYPE
import com.clean.filecleaner.ui.module.notification.BaseBarFunction
import com.clean.filecleaner.ui.module.notification.FuncClean
import com.clean.filecleaner.ui.module.notification.FuncScreenShot
import com.clean.filecleaner.utils.AndroidVersionUtils
import com.clean.filecleaner.utils.AppPreferences.hasRequestUMP
import com.clean.filecleaner.utils.UMPUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun setupImmersiveMode() = immersiveMode()
    override fun inflateViewBinding(): ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)

    private val barFunction by lazy { intent?.getParcelableExtra<BaseBarFunction>(FUNCTION_TYPE) }
    private var hasClickPermission = false
    private var countDownTimerIsEnd = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val notificationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        if (isGrantedNotification()) {
            BarNotificationCenter.init(this)
        }

        hasClickPermission = true
        if (countDownTimerIsEnd) {
            if (adManagerState.fcLaunchState.canShow()) {
                fullScreenAdShow { toNextPage() }
            } else toNextPage()
            adManagerState.fcMainNatState.loadAd(this)
        }

    }

    override fun initView(savedInstanceState: Bundle?) {

        DataReportingUtils.postSessionEvent()
        DataReportingUtils.postCustomEvent("loading_page")

        lifecycleScope.launch(Dispatchers.IO) {
            delay(3000L)
            BarNotificationCenter.init(this@SplashActivity)
        }

        if (!adManagerState.hasReachedUnusualAdLimit()) {
            DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_launch"))
        }

        if (BarNotificationCenter.isKorean().not() && isGrantedNotification().not() && AndroidVersionUtils.isAndroid13OrAbove()) {
            notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasClickPermission = true
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
            if (adManagerState.fcLaunchState.canShow() && interval > 20 && hasClickPermission) {

                job?.cancel()
                adManagerState.fcMainNatState.loadAd(this)

                fullScreenAdShow { toNextPage() }

            }
        }, end = {
            countDownTimerIsEnd = true
            if (hasClickPermission) {
                toNextPage()
                adManagerState.fcMainNatState.loadAd(this)
            }
        })
    }


    private fun toNextPage() {
        when (barFunction) {
            FuncScreenShot, FuncClean -> {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra(FUNCTION_TYPE, barFunction)
                })
                finish()
            }

            else -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
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
                delay(210L)
            }
            adState.showFullScreenAd(this@SplashActivity, "fc_launch", false) { onComplete() }
        }
    }

}

