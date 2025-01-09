package com.clean.filecleaner.report.reporter

import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.BuildConfig
import com.clean.filecleaner.report.DataReportingConfig
import com.clean.filecleaner.report.DataReportingConfig.ReportingIOScope
import com.clean.filecleaner.utils.AppPreferences.prefs
import com.clean.filecleaner.utils.prefDelegate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object CloakRepository {

    var cloakResult by prefs.prefDelegate("")

    fun getCloakInfo() {
        if (cloakResult.isNotBlank()) return
        DataReportingConfig.getCloakInfoJob = ReportingIOScope.launch {
            while (cloakResult.isBlank()) {
                delay(1000L)
                requestCloakInfo()
                delay(20_000L)
            }
        }
    }

    private fun requestCloakInfo() = runCatching {
        val obj = JSONObject().apply {
            put("slop", "com.viewer.document.pdf.reader.tools.application")
            put("sinbad", "meringue")
            put("youd", BuildConfig.VERSION_NAME)
            put("search", System.currentTimeMillis())
        }

        val body = obj.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().post(body).url(DataReportingConfig.cloakUrl).build()

        DataReportingConfig.httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                LogUtils.e("requestCloakInfo", "onFailure: ${e.message}==$obj")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val bodyStr = response.body?.string()
                    LogUtils.d("requestCloakInfo", "Cloak Result ==${bodyStr}")
                    if (!bodyStr.isNullOrBlank()) {
                        cloakResult = bodyStr
                        DataReportingConfig.getCloakInfoJob?.cancel()
                    }
                }
            }
        })
    }

}