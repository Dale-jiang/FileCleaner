package com.clean.filecleaner.ext

import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.BuildConfig
import com.clean.filecleaner.report.reporter.AdLTVReporter
import com.clean.filecleaner.report.reporter.AdLTVReporter.revenuePercentList
import com.clean.filecleaner.ui.ad.abnormalAdConfig
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.initData
import com.clean.filecleaner.ui.ad.local_ad_json
import com.clean.filecleaner.ui.ad.userCloConfig
import com.clean.filecleaner.ui.ad.userRefConfig
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter
import com.clean.filecleaner.ui.module.notification.NotificationCenter
import com.clean.filecleaner.ui.module.notification.NotificationConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.get
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONObject

val mRemoteConfig by lazy {
    Firebase.remoteConfig.apply {
        setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3500
        })
    }
}

data class AdAbnormalConfig(
    @SerializedName("augfauk") val open: Int,
    @SerializedName("veasv") val type: Int,
    @SerializedName("tebvs") val interval: Int,
    @SerializedName("xwaea") val maxShow: Int,
    @SerializedName("ijrtfh") val maxClick: Int
)

fun Firebase.initRemoteConfig() {

    fun getAdConfig() {
        runCatching {
            val json = mRemoteConfig["fc_ad_config"].asString()
            LogUtils.d("getAdConfig", "Result: $json")
            adManagerState.initData(json.ifBlank { local_ad_json })
        }.onFailure {
            adManagerState.initData(local_ad_json)
        }
    }

    fun getUserConf() {
        runCatching {
            val json = mRemoteConfig["fc_config_user"].asString()
            if (json.isNotEmpty()) {
                JSONObject(json).apply {
                    userRefConfig = optInt("fc_ref_ask")
                    userCloConfig = optInt("fc_clo_ask")
                }
            }
        }.onFailure {
            LogUtils.e("getUserConf", "getUserConf error!!!")
        }
    }

    fun getAdAbnormalConfig() {
        runCatching {
            val json = mRemoteConfig["sovjesj"].asString()
            if (json.isEmpty()) return
            runCatching {
                LogUtils.d("getAdAbnormalConfig", "Result: $json")
                abnormalAdConfig = Gson().fromJson(json, AdAbnormalConfig::class.java)
            }.onFailure { exception ->
                LogUtils.e("getAdAbnormalConfig", "Error: ${exception.message}", exception)
            }
        }.onFailure { exception ->
            LogUtils.e("getAdAbnormalConfig", "Error: ${exception.message}", exception)
        }
    }


    fun getNoticeConfig() {
        val isSamsung = BarNotificationCenter.isSamsungDevice()
        runCatching {
            val json = mRemoteConfig[if (isSamsung) "samsungpop" else "fc_pop"].asString()
            if (json.isNotEmpty()) {
                NotificationCenter.noticeConfig = Gson().fromJson(json, NotificationConfig::class.java)
            } else {
                NotificationCenter.noticeConfig = if (isSamsung) {
                    NotificationConfig(1, 30, 35, 10, 15, 30, 5, 30, 5)
                } else {
                    NotificationConfig(1, 30, 40, 10, 20, 30, 10, 30, 10)
                }
            }
        }.onFailure {
            NotificationCenter.noticeConfig = if (isSamsung) {
                NotificationConfig(1, 30, 35, 10, 15, 30, 5, 30, 5)
            } else {
                NotificationConfig(1, 30, 40, 10, 20, 30, 10, 30, 10)
            }
        }

    }


     fun fetchTopPercentConf() = runCatching {
        val json = remoteConfig["FIleCleaner_toppercent"].asString()
        if (json.isBlank()) return@runCatching
        JSONObject(json).apply {
            AdLTVReporter.run {
                revenuePercentList = mutableListOf(
                    optDouble("fc_adltv_top50", 0.0),
                    optDouble("fc_adltv_top40", 0.0),
                    optDouble("fc_adltv_top30", 0.0),
                    optDouble("fc_adltv_top20", 0.0),
                    optDouble("fc_adltv_top10", 0.0),
                )
            }
        }
    }


    fun getAllConfigs() {
        getAdConfig()
        getAdAbnormalConfig()
        getUserConf()
        getNoticeConfig()
        fetchTopPercentConf()
    }


    if (BuildConfig.DEBUG) {
        adManagerState.initData()
        getNoticeConfig()
    } else {
        getAllConfigs()
        mRemoteConfig.fetchAndActivate().addOnSuccessListener { getAllConfigs() }
    }
}