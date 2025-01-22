package com.clean.filecleaner.report.reporter

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.clean.filecleaner.utils.AppPreferences.topPercentDatetime
import com.clean.filecleaner.utils.AppPreferences.topPercentRevenue
import com.clean.filecleaner.utils.AppPreferences.total001Revenue
import com.google.firebase.analytics.FirebaseAnalytics

object AdLTVReporter {

    private const val TAG = "AdLTVReporter"
    var revenuePercentList = listOf<Double>()


    fun post(revenue: Double) {
        postGoogle20(revenue)
        postGoogle30(revenue)
    }

    private fun postGoogle20(revenue: Double) = kotlin.run {

        val currentTime = System.currentTimeMillis()
        val thresholdsWithEvents = listOf(
            revenuePercentList[0] to "AdLTV_Top50Percent",
            revenuePercentList[1] to "AdLTV_Top40Percent",
            revenuePercentList[2] to "AdLTV_Top30Percent",
            revenuePercentList[3] to "AdLTV_Top20Percent",
            revenuePercentList[4] to "AdLTV_Top10Percent"
        ).sortedByDescending { it.first }

        if (!TimeUtils.isToday(topPercentDatetime)) {
            topPercentRevenue = 0.0
        }

        topPercentDatetime = currentTime
        val previousRevenue = topPercentRevenue
        topPercentRevenue += revenue

        if (thresholdsWithEvents.any { it.first <= 0.0 }) return

        thresholdsWithEvents.forEach { (threshold, eventName) ->
            if (previousRevenue < threshold && topPercentRevenue >= threshold) {
                try {
                    DataReportingUtils.postCustomEvent(eventName, hashMapOf(FirebaseAnalytics.Param.VALUE to threshold, FirebaseAnalytics.Param.CURRENCY to "USD"))
                } catch (e: Exception) {
                    LogUtils.e(TAG, "Failed to post event '$eventName': ${e.message}")
                }
            }
        }

    }

    private fun postGoogle30(revenue: Double) = kotlin.run {

        val updatedRevenue = total001Revenue + revenue

        if (updatedRevenue >= 0.01) {
            runCatching {
                total001Revenue = 0.0
                DataReportingUtils.postCustomEvent("TotalAdRevenue001", hashMapOf(FirebaseAnalytics.Param.VALUE to updatedRevenue, FirebaseAnalytics.Param.CURRENCY to "USD"))
            }.onFailure { exception ->
                LogUtils.e(TAG, "Failed to post event: ${exception.message}")
            }
        } else {
            total001Revenue = updatedRevenue
        }

    }

}