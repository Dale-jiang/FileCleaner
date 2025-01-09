package com.clean.filecleaner.report

import android.content.Context
import android.telephony.TelephonyManager
import com.clean.filecleaner.BuildConfig
import com.clean.filecleaner.data.app
import com.clean.filecleaner.utils.AppPreferences.distinctId
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.UUID

object DataReportingConfig {

    val ReportingIOScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }) }

    val cloakUrl = "https://slick.filecleanerapp.com/curbside/ashman/joel"

    val tbaUrl = if (BuildConfig.DEBUG) {
        "https://test-wert.filecleanerapp.com/theory/chain"
    } else {
        "https://wert.filecleanerapp.com/duration/carbon"
    }

    val mDistinctId by lazy { createDistinctId() }
    val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(app) }
    val facebookLogger by lazy { AppEventsLogger.newLogger(app) }

    // HttpClient
    val httpClient by lazy { OkHttpClient.Builder().build() }

    // 全局常量
    const val MAX_RETRIES = 4
    const val RETRY_DELAY_MILLIS = 60000L

    var getCloakInfoJob: Job? = null
    var getReferrerJob: Job? = null
    var googleInfoJob: Job? = null

    private fun createDistinctId(): String = let {
        var uuid = distinctId
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString().replace("-", "")
            distinctId = uuid
        }
        uuid
    }

    fun getOperator(): String = (app.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.networkOperator ?: ""

}