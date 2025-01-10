package com.clean.filecleaner.ui.ad

import androidx.annotation.Keep
import com.clean.filecleaner.ext.AdAbnormalConfig
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

val buyUserTags by lazy { arrayOf("fb4a", "instagram", "ig4a", "gclid", "not%20set", "youtubeads", "%7B%22", "adjust", "bytedance") }

var userRefConfig = 0
var userCloConfig = 0
var abnormalAdConfig = AdAbnormalConfig(open = 1, type = 1, interval = 24, maxShow = 20, maxClick = 12)
var canShowBackAd = false
val adManagerState by lazy { AdManagerState() }

val local_ad_json = """
    {
      "fc_launch": [
        {
          "yuvceh": "ca-app-pub-3940256099942544/9257395921",
          "suwf": "admob",
          "cxuaw": "op",
          "caiuh": 13800,
          "aiuhca": 3
        }
      ],
        "fc_backscan_int": [
        {
          "yuvceh": "ca-app-pub-3940256099942544/8691691433",
          "suwf": "admob",
          "cxuaw": "int",
          "caiuh": 3000,
          "aiuhca": 3
        }
      ],
      "fc_result_int": [
        {
          "yuvceh": "ca-app-pub-3940256099942544/8691691433",
          "suwf": "admob",
          "cxuaw": "int",
          "caiuh": 3000,
          "aiuhca": 3
        }
      ],
      "fc_main_nat": [
        {
          "yuvceh": "ca-app-pub-3940256099942544/1044960115",
          "suwf": "admob",
          "cxuaw": "nat",
          "caiuh": 3000,
          "aiuhca": 3
         }
      ],
      "fc_scan_nat": [
        {
          "yuvceh": "ca-app-pub-3940256099942544/1044960115",
          "suwf": "admob",
          "cxuaw": "nat",
          "caiuh": 3000,
          "aiuhca": 3
        }
      ],
      "fc_result_nat": [
        {
          "yuvceh": "ca-app-pub-3940256099942544/1044960115",
          "suwf": "admob",
          "cxuaw": "nat",
          "caiuh": 3000,
          "aiuhca": 3
        }
      ]
    }
""".trimIndent()


@Keep
data class AdConfigList(
    @SerializedName("fc_launch")
    val fcLaunch: List<AdConfigDetails> = emptyList(),

    @SerializedName("fc_backscan_int")
    val fcBackScanInt: List<AdConfigDetails> = emptyList(),

    @SerializedName("fc_result_int")
    val fcResultInt: List<AdConfigDetails> = emptyList(),

    @SerializedName("fc_main_nat")
    val fcMainNat: List<AdConfigDetails> = emptyList(),

    @SerializedName("fc_scan_nat")
    val fcScanNat: List<AdConfigDetails> = emptyList(),

    @SerializedName("fc_result_nat")
    val fcResultNat: List<AdConfigDetails> = emptyList()
)

@Keep
data class AdConfigDetails(
    @SerializedName("yuvceh")
    val id: String,
    @SerializedName("suwf")
    val platform: String,
    @SerializedName("cxuaw")
    val type: String,
    @SerializedName("aiuhca")
    val weight: Int,
    @SerializedName("caiuh")
    val expireTime: Int
)

data class AdManagerState(
    var adConfigList: AdConfigList? = null,
    var fcLaunchState: AdWithFullScreenState = AdWithFullScreenState("fc_launch"),
    var fcBackScanIntState: AdWithFullScreenState = AdWithFullScreenState("fc_backscan_int"),
    var fcResultIntState: AdWithFullScreenState = AdWithFullScreenState("fc_result_int"),
    var fcMainNatState: AdWithNativeState = AdWithNativeState("fc_main_nat"),
    var fcScanNatState: AdWithNativeState = AdWithNativeState("fc_scan_nat"),
    var fcResultNatState: AdWithNativeState = AdWithNativeState("fc_result_nat"),
    var gson: Gson = Gson()
)

data class AdWithNativeState(
    val adLocation: String,
    var onLoaded: ((Boolean) -> Unit)? = null,
    val configDetails: MutableList<AdConfigDetails> = mutableListOf(),
    val realAdList: MutableList<IAd> = mutableListOf(),
    var isLoading: Boolean = false
)

data class AdWithFullScreenState(
    val adLocation: String,
    var onLoaded: ((Boolean) -> Unit)? = null,
    val configDetails: MutableList<AdConfigDetails> = mutableListOf(),
    val realAdList: MutableList<IAd> = mutableListOf(),
    var isLoading: Boolean = false
)