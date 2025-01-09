package com.clean.filecleaner.report.reporter

import android.os.Build
import android.os.Bundle
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.BuildConfig
import com.clean.filecleaner.report.DataReportingConfig
import com.clean.filecleaner.report.DataReportingConfig.ReportingIOScope
import com.clean.filecleaner.report.DataReportingConfig.getOperator
import com.clean.filecleaner.report.DataReportingConfig.mDistinctId
import kotlinx.coroutines.CompletableDeferred
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
import java.util.Locale
import java.util.UUID

object EventReporter {

    private fun HashMap<String, Any?>.toBundle(): Bundle? {
        if (isEmpty()) return null
        return Bundle().apply {
            forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                }
            }
        }
    }


    fun eventPost(event: String, params: HashMap<String, Any?> = hashMapOf()) {
        runCatching {
            if (params.isNotEmpty()) {
                LogUtils.d("eventPost", "EventName: $event, Params: $params")
            } else {
                LogUtils.d("eventPost", "EventName: $event")
            }
            if (!BuildConfig.DEBUG) {
                DataReportingConfig.firebaseAnalytics.logEvent(event, params.toBundle())
            }
            postEvent(event, params)
        }
    }

    private fun postEvent(event: String, params: HashMap<String, Any?>) {
        ReportingIOScope.launch {
            val jsonObj = buildCommonParams().apply {
                put("bluegill", event)
                put(event, JSONObject().apply {
                    params.forEach { (k, v) -> put("${k}<college", v) }
                })
            }
            runRequest(jsonObj.toString(), event)
        }
    }

    fun buildCommonParams(): JSONObject {
        return JSONObject().apply {

            put("slop", "com.filecleaner.app.optimization.cleaningtool.utilities")
            put("abort", Build.MODEL ?: "")
            put("magician", mDistinctId)
            put("sinbad", "meringue")
            put("search", System.currentTimeMillis())
            put("late", getOperator())
            put("success", UUID.randomUUID().toString())
            put("roast", Locale.getDefault().toString())
            put("penitent", Build.MANUFACTURER ?: "")
            put("youd", BuildConfig.VERSION_NAME)
            put("diem", Build.VERSION.RELEASE ?: "")

        }
    }


    suspend fun runRequest(bodyString: String, requestTag: String) {
        val body = bodyString.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder().post(body).url(DataReportingConfig.tbaUrl).build()
        executeWithRetry(request, requestTag)
    }

    private suspend fun executeWithRetry(request: Request, tag: String) {
        var attempt = 0
        while (attempt < DataReportingConfig.MAX_RETRIES) {
            val deferred = CompletableDeferred<Response?>()
            DataReportingConfig.httpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    LogUtils.e("onFailure", "$tag: ${e.message}")
                    deferred.completeExceptionally(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    LogUtils.d("onResponse", "$tag: ${response.code}, ${response.body?.string() ?: ""}")
                    if (response.isSuccessful) {
                        deferred.complete(response)
                    } else {
                        deferred.complete(null)
                    }
                }
            })

            try {
                val response = deferred.await()
                if (response != null) {
                    LogUtils.d("$tag: Request success")
                    return
                }
            } catch (e: IOException) {
                LogUtils.e("$tag: Request failed: ${e.message}")
            }

            attempt++
            if (attempt < DataReportingConfig.MAX_RETRIES) {
                LogUtils.d("Retrying: $tag", "Attempt: $attempt Retrying in ${DataReportingConfig.RETRY_DELAY_MILLIS} ms")
                delay(DataReportingConfig.RETRY_DELAY_MILLIS)
            }
        }
    }

}