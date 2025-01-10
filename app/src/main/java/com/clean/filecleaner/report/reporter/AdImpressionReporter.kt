package com.clean.filecleaner.report.reporter

import com.clean.filecleaner.report.DataReportingConfig
import com.clean.filecleaner.ui.ad.IAd
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo
import kotlinx.coroutines.launch
import org.json.JSONObject

object AdImpressionReporter {

    private fun getPrecisionType(precisionTypeCode: Int) = let {
        when (precisionTypeCode) {
            AdValue.PrecisionType.PUBLISHER_PROVIDED -> "PUBLISHER_PROVIDED"
            AdValue.PrecisionType.ESTIMATED -> "ESTIMATED"
            AdValue.PrecisionType.PRECISE -> "PRECISE"
            else -> "UNKNOWN"
        }
    }

    fun postAdImpression(adValue: AdValue, responseInfo: ResponseInfo?, ad: IAd) {
        runCatching {
            val jsonObj = EventReporter.buildCommonParams().apply {
                put("mamma", JSONObject().apply {
                    put("unimodal", getPrecisionType(adValue.precisionType))
                    put("sac", getAdTypeName(ad.getAdConfigDetail()?.type ?: ""))
                    put("subvert", ad.getAdConfigDetail()?.id ?: "")
                    put("caught", ad.getAdConfigDetail()?.platform ?: "admob")
                    put("glue", ad.getAdLocationStr())
                    put("zinnia", getAdapterClassName(responseInfo))
                    put("boot", adValue.currencyCode)
                    put("butene", adValue.valueMicros)
                })
            }
            DataReportingConfig.ReportingIOScope.launch {
                EventReporter.runRequest(jsonObj.toString(), "logAdImpression")
            }
        }

    }

    private fun getAdTypeName(adType: String): String {
        return when (adType) {
            "op" -> "open"
            "int" -> "interstitial"
            "nat" -> "native"
            "" -> "UNKNOWN"
            else -> "UNKNOWN"
        }
    }

    private fun getAdapterClassName(responseInfo: ResponseInfo?) = runCatching {
        val mediationClzName = responseInfo?.mediationAdapterClassName ?: "admob"
        when {
            mediationClzName.contains("applovin", true) -> "applovin"
            mediationClzName.contains("pangle", true) -> "pangle"
            mediationClzName.contains("facebook", true) -> "facebook"
            mediationClzName.contains("mintegral", true) -> "mintegral"
            mediationClzName.contains("mbridge.msdk", true) -> "mintegral"
            else -> "admob"
        }
    }.getOrDefault("admob")
}