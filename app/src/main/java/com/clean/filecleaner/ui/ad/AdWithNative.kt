package com.clean.filecleaner.ui.ad

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.databinding.ViewNativeAdBinding
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.base.BaseActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

class AdWithNative(private val adLocation: String?, private val adConfigDetails: AdConfigDetails?) : IAd {

    private val tag = "AdWithNative"
    private var nativeAd: NativeAd? = null
    private val adRequest by lazy { AdRequest.Builder().build() }
    private var adLoadTime: Long = System.currentTimeMillis()
    var posId = ""

    override fun getAdLocationStr(): String {
        return adLocation ?: ""
    }

    override fun getAdConfigDetail(): AdConfigDetails? {
        return adConfigDetails
    }

    override fun loadAd(context: Context, onLoaded: (success: Boolean, msg: String?) -> Unit) {
        val configDetails = adConfigDetails ?: return onLoaded(false, "adConfigDetails is null")
        LogUtils.d(tag, "[${adLocation}] ${configDetails.type} - ${configDetails.id}, start loading (NativeAd)")
        AdLoader.Builder(app, configDetails.id).apply {
            forNativeAd { ad ->
                nativeAd = ad
                adLoadTime = System.currentTimeMillis()
                ad.setOnPaidEventListener {
                    DataReportingUtils.postCustomEvent("fc_ad_impression", hashMapOf("ad_pos_id" to posId))
                    AdUtils.onPaidEventListener(it, ad.responseInfo, this@AdWithNative)
                }
                onLoaded(true, "")
            }
            withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(e: LoadAdError) = onLoaded(false, e.message)
                override fun onAdClicked() {
                    DataReportingUtils.postCustomEvent("ad_click_n")
                    adManagerState.updateUnusualAdInfo(isFullAd = false, isClick = true)
                }
            })
            withNativeAdOptions(NativeAdOptions.Builder().setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_LEFT).build())
        }.build().loadAd(adRequest)

    }

    override fun showAd(activity: BaseActivity<*>, parent: ViewGroup?, onClose: () -> Unit) {

        nativeAd?.let { ad ->
            val binding = ViewNativeAdBinding.inflate(LayoutInflater.from(activity), parent, false)
            binding.viewNativeAd.apply {
                iconView = binding.logo.apply {
                    setImageDrawable(ad.icon?.drawable)
                }
                mediaView = binding.mediaView.apply {
                    mediaContent = ad.mediaContent
                    setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                }
                headlineView = binding.name.apply {
                    text = ad.headline.orEmpty()
                }

                bodyView = binding.subName.apply {
                    text = ad.body.orEmpty()
                }

                callToActionView = binding.btnAction.apply {
                    text = ad.callToAction.orEmpty()
                }
                setNativeAd(ad)
            }

            parent?.apply {
                removeAllViews()
                addView(binding.root)
            }

            adManagerState.updateUnusualAdInfo(isFullAd = false, isClick = false)

            LogUtils.d("$adLocation - ${adConfigDetails?.type} - ${adConfigDetails?.id} show success (nativeAd)")
        }


    }

    override fun isAdExpire(): Boolean {
        val configDetails = adConfigDetails ?: return false
        val diff = System.currentTimeMillis() - adLoadTime
        return diff >= configDetails.expireTime * 1000L
    }

    override fun destroy() {
        nativeAd?.destroy()
    }


}