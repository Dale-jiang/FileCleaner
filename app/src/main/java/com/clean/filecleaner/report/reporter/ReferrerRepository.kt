package com.clean.filecleaner.report.reporter

import android.os.Build
import android.webkit.WebSettings
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.getFirInstallTime
import com.clean.filecleaner.ext.getLastUpdateTime
import com.clean.filecleaner.report.DataReportingConfig
import com.clean.filecleaner.report.DataReportingConfig.ReportingIOScope
import com.clean.filecleaner.report.reporter.EventReporter.buildCommonParams
import com.clean.filecleaner.report.reporter.EventReporter.runRequest
import com.clean.filecleaner.report.reporter.GoogleInfoRepository.enableLimitAdTracker
import com.clean.filecleaner.utils.AppPreferences.prefs
import com.clean.filecleaner.utils.prefDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

object ReferrerRepository {

    var installReferrerStr by prefs.prefDelegate("")

    fun getReferrerInfo() {
        if (installReferrerStr.isNotBlank()) return
        DataReportingConfig.getReferrerJob = ReportingIOScope.launch {
            while (installReferrerStr.isBlank()) {
                delay(1000L)
                requestReferrer()
                delay(25_000L)
            }
        }
    }

    private fun requestReferrer() {
        runCatching {
            val referrerClient = InstallReferrerClient.newBuilder(app).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    try {
                        if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                            val referrerDetails = referrerClient.installReferrer
                            val referrer = referrerDetails?.installReferrer ?: ""
                            if (referrer.isNotBlank()) {
                                installReferrerStr = referrer
                                DataReportingConfig.getReferrerJob?.cancel()
                            }
                            if (installReferrerStr.isNotBlank()) {
                                postInstall(referrerDetails)
                                DataReportingConfig.getReferrerJob?.cancel()
                            }
                        }
                    } finally {
                        referrerClient.endConnection()
                    }
                }

                override fun onInstallReferrerServiceDisconnected() = Unit
            })
        }
    }

    private fun postInstall(referrerInfo: ReferrerDetails?) {
        ReportingIOScope.launch {
            val parameters = buildCommonParams()
            parameters.apply {
                put("tweedy", JSONObject().apply {
                    put("piggy", app.getLastUpdateTime())
                    put("oat", app.getFirInstallTime())
                    put("aching", referrerInfo?.installBeginTimestampServerSeconds ?: 0L)
                    put("ted", referrerInfo?.referrerClickTimestampServerSeconds ?: 0L)
                    put("jostle", referrerInfo?.installBeginTimestampSeconds ?: 0L)
                    put("godfrey", referrerInfo?.referrerClickTimestampSeconds ?: 0L)
                    put("kickoff", if (enableLimitAdTracker) "leprosy" else "spitfire")
                    put("aberrate", WebSettings.getDefaultUserAgent(app) ?: "")
                    put("everyday", referrerInfo?.installVersion ?: "")
                    put("aver", referrerInfo?.installReferrer ?: "")
                    put("goose", "build/${Build.ID}")
                })
            }
            runRequest(parameters.toString(), "logInstallEvent")
        }
    }

}