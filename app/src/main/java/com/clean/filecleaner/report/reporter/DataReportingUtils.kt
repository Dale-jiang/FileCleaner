package com.clean.filecleaner.report.reporter

import com.clean.filecleaner.ui.ad.IAd
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo

object DataReportingUtils {

    fun getAllInfos() = runCatching {
        GoogleInfoRepository.getGoogleInfo()
        CloakRepository.getCloakInfo()
        ReferrerRepository.getReferrerInfo()
    }

    fun postSessionEvent() {
        SessionReporter.postSession()
    }

    fun postAdImpressionEvent(adValue: AdValue, responseInfo: ResponseInfo?, ad: IAd) {
        AdImpressionReporter.postAdImpression(adValue, responseInfo, ad)
    }

    fun eventPost(event: String, params: HashMap<String, Any?> = hashMapOf()) {
        EventReporter.eventPost(event, params)
    }

}