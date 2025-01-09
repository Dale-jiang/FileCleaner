package com.clean.filecleaner.report.reporter

import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.report.DataReportingConfig
import com.clean.filecleaner.report.DataReportingConfig.ReportingIOScope
import com.clean.filecleaner.utils.AppPreferences.prefs
import com.clean.filecleaner.utils.prefDelegate
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object GoogleInfoRepository {

    private var gaidString by prefs.prefDelegate("")
    var enableLimitAdTracker by prefs.prefDelegate(true)

    fun getGoogleInfo() {
        if (gaidString.isNotBlank()) return
        DataReportingConfig.googleInfoJob = ReportingIOScope.launch {
            while (gaidString.isBlank()) {
                delay(500L)
                doGet()
                delay(25_000L)
            }
        }
    }

    private fun doGet() {
        runCatching {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(app)
            val gaid = adInfo.id ?: ""
            if (gaid.isNotBlank()) {
                gaidString = gaid
                adInfo.isLimitAdTrackingEnabled.also {
                    enableLimitAdTracker = it
                }
                LogUtils.d("getGaid success: $gaid")
                DataReportingConfig.googleInfoJob?.cancel()
            }
        }
    }

}