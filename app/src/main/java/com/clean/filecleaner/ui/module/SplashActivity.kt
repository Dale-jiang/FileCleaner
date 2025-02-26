package com.clean.filecleaner.ui.module

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.TimeUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.databinding.ActivitySplashBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.isDarkMode
import com.clean.filecleaner.ext.isGrantedNotification
import com.clean.filecleaner.ext.isOverlayPermissionGranted
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter.FUNCTION_TYPE
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter.NOTICE_INFO_ITEM
import com.clean.filecleaner.ui.module.notification.BaseBarFunction
import com.clean.filecleaner.ui.module.notification.FuncClean
import com.clean.filecleaner.ui.module.notification.FuncScreenShot
import com.clean.filecleaner.ui.module.notification.InstallReminder
import com.clean.filecleaner.ui.module.notification.NotificationInfo
import com.clean.filecleaner.ui.module.notification.TaskReminder
import com.clean.filecleaner.ui.module.notification.UninstallReminder
import com.clean.filecleaner.ui.module.notification.UserPresenceReminder
import com.clean.filecleaner.utils.AndroidVersionUtils
import com.clean.filecleaner.utils.AppPreferences.floatingPermissionPageTime
import com.clean.filecleaner.utils.AppPreferences.hasRequestUMP
import com.clean.filecleaner.utils.Tools.isBlackUser
import com.clean.filecleaner.utils.Tools.isBuyUser
import com.clean.filecleaner.utils.UMPUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    override fun setupImmersiveMode() = immersiveMode()
    override fun inflateViewBinding(): ActivitySplashBinding = ActivitySplashBinding.inflate(layoutInflater)

    //    private val reminder by lazy { intent?.getParcelableExtra<BaseReminder>(REMINDER_TYPE) }
    private val mNoticeInfo by lazy { intent?.getParcelableExtra<NotificationInfo>(NOTICE_INFO_ITEM) }
    private val barFunction by lazy { intent?.getParcelableExtra<BaseBarFunction>(FUNCTION_TYPE) }
    private var hasClickPermission = false
    private var countDownTimerIsEnd = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val notificationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        if (isGrantedNotification()) {
            BarNotificationCenter.init(this)
            DataReportingUtils.postCustomEvent("PermYes")
        } else {
            DataReportingUtils.postCustomEvent("PermNo")
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
        DataReportingUtils.postCustomEvent("LoadPage")

        lifecycleScope.launch(Dispatchers.IO) {
            delay(3000L)
            BarNotificationCenter.init(this@SplashActivity)
        }

        if (!adManagerState.hasReachedUnusualAdLimit()) {
            DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_launch"))
        }

        if (isGrantedNotification().not() && AndroidVersionUtils.isAndroid13OrAbove()) {
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

        closeNotice()
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
        val function = if (barFunction == null) mNoticeInfo?.function else barFunction
        when (function) {
            FuncScreenShot, FuncClean -> {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    putExtra(FUNCTION_TYPE, function)
                })
                finish()
            }

            else -> {

                if (!isBlackUser() && isBuyUser() && !TimeUtils.isToday(floatingPermissionPageTime) && !isOverlayPermissionGranted()) {
                    floatingPermissionPageTime = System.currentTimeMillis()
                    startActivity(Intent(this, FloatingWindowPermissionActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
        }
    }

    private fun closeNotice() {

        if (barFunction != null || mNoticeInfo != null) {
            if ("0" != mNoticeInfo?.temp) {
                DataReportingUtils.postCustomEvent("PopAllClick")
            }
        }

        mNoticeInfo?.apply {
            if ("0" != this.temp) {
                when (this.reminder) {
                    InstallReminder -> DataReportingUtils.postCustomEvent("PopAddClick")
                    TaskReminder -> DataReportingUtils.postCustomEvent("PopTimerClick")
                    UninstallReminder -> DataReportingUtils.postCustomEvent("PopUniqueClick")
                    UserPresenceReminder -> DataReportingUtils.postCustomEvent("PopUnlockClick")
                }

                if (app.isDarkMode()) {
                    DataReportingUtils.postCustomEvent("PopDarkClick")
                }
            }
        }


        runCatching {
            mNoticeInfo?.apply {
                NotificationManagerCompat.from(app).cancel(notificationId)
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

