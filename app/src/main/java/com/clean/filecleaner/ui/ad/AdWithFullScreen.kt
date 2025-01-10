package com.clean.filecleaner.ui.ad

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.base.BaseActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class AdWithFullScreen(val adLocation: String?, private val adConfigDetails: AdConfigDetails?) : IAd {

    private val tag = "AdWithFullScreen"
    private val adRequest by lazy { AdRequest.Builder().build() }
    private var realAd: Any? = null
    private var adLoadTime: Long = System.currentTimeMillis()

    override fun getAdLocationStr(): String {
        return adLocation ?: ""
    }

    override fun getAdConfigDetail(): AdConfigDetails? {
        return adConfigDetails
    }

    override fun loadAd(context: Context, onLoaded: (success: Boolean, msg: String?) -> Unit) {
        when (adConfigDetails?.type) {
            "op" -> loadOpenAd(onLoaded)
            "int" -> loadInterstitialAd(onLoaded)
            else -> onLoaded(false, "Invalid adType: ${adConfigDetails?.type}")
        }
    }

    private fun loadOpenAd(onLoaded: (Boolean, String?) -> Unit) {

        val configDetails = adConfigDetails ?: return onLoaded(false, "adConfigDetails is null")
        LogUtils.d(tag, "[${adLocation}] ${configDetails.type} - ${configDetails.id}, start loading (OpenAd)")

        AppOpenAd.load(app, configDetails.id, adRequest, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                onLoaded(false, error.message)
            }

            override fun onAdLoaded(openAd: AppOpenAd) {
                adLoadTime = System.currentTimeMillis()
                realAd = openAd
                openAd.setOnPaidEventListener {
                    AdUtils.onPaidEventListener(it, openAd.responseInfo, this@AdWithFullScreen)
                }
                onLoaded(true, null)
            }
        })
    }

    private fun loadInterstitialAd(onLoaded: (Boolean, String?) -> Unit) {

        val configDetails = adConfigDetails ?: return onLoaded(false, "adConfigDetails is null")
        LogUtils.d(tag, "[${adLocation}] ${configDetails.type} - ${configDetails.id}, start loading (InterstitialAd)")

        InterstitialAd.load(app, configDetails.id, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                onLoaded(false, error.message)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                adLoadTime = System.currentTimeMillis()
                realAd = interstitialAd
                interstitialAd.setOnPaidEventListener {
                    AdUtils.onPaidEventListener(it, interstitialAd.responseInfo, this@AdWithFullScreen)
                }
                onLoaded(true, null)
            }
        })
    }


    override fun showAd(activity: BaseActivity<*>, parent: ViewGroup?, onClose: () -> Unit) {

        val configDetails = adConfigDetails

        val callback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onAdClose(activity, onClose)
            }

            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                LogUtils.d(tag, "[${adLocation}] ${configDetails?.type} - ${configDetails?.id}, failed: ${e.message}")
                onAdClose(activity, onClose)
            }

            override fun onAdClicked() {
                DataReportingUtils.postCustomEvent("ad_click_v")
                adManagerState.updateUnusualAdInfo(isFullAd = true, isClick = true)
            }

            override fun onAdShowedFullScreenContent() {
                LogUtils.d(tag, "[${adLocation}] ${configDetails?.type} - ${configDetails?.id}, showed successfully")
                adManagerState.updateUnusualAdInfo(isFullAd = true, isClick = false)
            }
        }

        when (val ad = realAd) {
            is InterstitialAd -> {
                ad.fullScreenContentCallback = callback
                ad.show(activity)
            }

            is AppOpenAd -> {
                ad.fullScreenContentCallback = callback
                ad.show(activity)
            }

            else -> {
                onAdClose(activity, onClose)
            }
        }
    }

    private fun onAdClose(activity: BaseActivity<*>?, onClose: () -> Unit) {
        activity?.lifecycleScope?.launch {
            while (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                delay(110L)
            }
            onClose()
        } ?: onClose()
    }

    override fun isAdExpire(): Boolean {
        val configDetails = adConfigDetails ?: return false
        val diff = System.currentTimeMillis() - adLoadTime
        return diff >= configDetails.expireTime * 1000L
    }

    override fun destroy() = Unit
}