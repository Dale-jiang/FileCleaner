package com.clean.filecleaner.ui.ad

import android.os.Bundle
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.clean.filecleaner.BuildConfig
import com.clean.filecleaner.report.DataReportingConfig
import com.clean.filecleaner.report.reporter.AdLTVReporter
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.Currency

object AdUtils {

    fun onPaidEventListener(adValue: AdValue, responseInfo: ResponseInfo?, ad: IAd) {

        DataReportingUtils.postAdImpressionEvent(adValue, responseInfo, ad)

        runCatching {
            val revenue: Double = adValue.valueMicros / 1000000.toDouble()

            if (!BuildConfig.DEBUG) {
                DataReportingConfig.facebookLogger.logPurchase(revenue.toBigDecimal(), Currency.getInstance("USD"))
                DataReportingConfig.firebaseAnalytics.logEvent("ad_impression_revenue", Bundle().apply {
                    putDouble(FirebaseAnalytics.Param.VALUE, revenue)
                    putString(FirebaseAnalytics.Param.CURRENCY, "USD")
                })

                runCatching {
                    val adRevenueData = AFAdRevenueData(responseInfo?.loadedAdapterResponseInfo?.adSourceName ?: "", MediationNetwork.GOOGLE_ADMOB, "USD", revenue)
                    val additionalParameters = HashMap<String, Any>().apply {
                        put(AdRevenueScheme.COUNTRY, "US")
                        put(AdRevenueScheme.AD_UNIT, ad.getAdConfigDetail()?.id ?: "")
                        put(AdRevenueScheme.AD_TYPE, ad.getAdConfigDetail()?.type ?: "")
                        put(AdRevenueScheme.PLACEMENT, ad.getAdLocationStr())
                    }
                    AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
                }
            }

            AdLTVReporter.post(revenue)
        }

    }

}