package com.clean.filecleaner.ui.ad

import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.getFirInstallTime
import com.clean.filecleaner.report.reporter.CloakRepository.cloakResult
import com.clean.filecleaner.report.reporter.ReferrerRepository.installReferrerStr
import com.clean.filecleaner.utils.AppPreferences.appInstallTime
import com.clean.filecleaner.utils.AppPreferences.isUnusualUser
import com.clean.filecleaner.utils.AppPreferences.unusualAdClickCount
import com.clean.filecleaner.utils.AppPreferences.unusualAdShowCount
import java.util.concurrent.TimeUnit

fun AdManagerState.initData(json: String = local_ad_json) {
    runCatching {
        adConfigList = gson.fromJson(json, AdConfigList::class.java)
    }.onFailure {
        LogUtils.e("initAdData", it.message ?: "Error")
    }
    dispatcherData()
}

fun AdManagerState.dispatcherData() {
    runCatching {
        adConfigList?.apply {
            fcLaunchState.initAdConfigDetails(fcLaunch)
            fcBackScanIntState.initAdConfigDetails(fcBackScanInt)
            fcResultIntState.initAdConfigDetails(fcResultInt)
            fcMainNatState.initAdConfigDetails(fcMainNat)
            fcScanNatState.initAdConfigDetails(fcScanNat)
            fcResultNatState.initAdConfigDetails(fcResultNat)
        }
    }
}

fun AdManagerState.updateUnusualAdInfo(isFullAd: Boolean, isClick: Boolean) {
    runCatching {
        if (abnormalAdConfig.open == 0) return@runCatching

        val isTypeMatching = (abnormalAdConfig.type == 1 && isFullAd) || abnormalAdConfig.type == 0
        if (!isTypeMatching) return@runCatching

        val currentTime = System.currentTimeMillis()
        val appInstallTimeTemp = if (appInstallTime == 0L) app.getFirInstallTime() else appInstallTime
        val hoursSinceInstall = TimeUnit.MILLISECONDS.toHours(currentTime - appInstallTimeTemp)

        when {

            hoursSinceInstall >= abnormalAdConfig.interval -> {
                unusualAdShowCount = if (isClick) 0 else 1
                unusualAdClickCount = if (isClick) 1 else 0
                appInstallTime = currentTime
            }

            else -> {
                if (isClick) {
                    unusualAdClickCount++
                    LogUtils.d("unusualAdClickCount == $unusualAdClickCount")
                } else {
                    unusualAdShowCount++
                    LogUtils.d("unusualAdShowCount == $unusualAdShowCount")
                }
                hasReachedUnusualAdLimit()
            }
        }

    }
}

fun AdManagerState.hasReachedUnusualAdLimit(): Boolean {
    if (isUnusualUser) return true
    val hasReachedLimit =
        unusualAdClickCount >= abnormalAdConfig.maxClick || unusualAdShowCount >= abnormalAdConfig.maxShow
    if (hasReachedLimit) {
        //   TbaHelper.eventPost("ad_abnormal_user")
        isUnusualUser = true
        return true
    }
    return false
}

fun AdManagerState.isBlocked() = when {
    userCloConfig == 1 && cloakResult == "engineer" -> true
    userRefConfig == 0 -> false
    else -> !buyUserTags.any { installReferrerStr.contains(it, ignoreCase = true) }
}