package com.clean.filecleaner.ui.ad

import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.dialog.AdLoadingDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


fun AdWithFullScreenState.canShow(): Boolean {
    return realAdList.isNotEmpty()
}

fun AdWithFullScreenState.initAdConfigDetails(list: List<AdConfigDetails>?) {
    configDetails.run {
        clear()
        list?.run { addAll(this) }
        sortByDescending { it.weight }
    }
}

fun AdWithFullScreenState.removeExpireAd() {
    runCatching {
        realAdList.firstOrNull()?.takeIf { it.isAdExpire() }?.let {
            realAdList.remove(it)
        }
    }
}

fun AdWithFullScreenState.loadAd(context: Context = app) {
    if (configDetails.isEmpty()) return
    if (adManagerState.hasReachedUnusualAdLimit()) return
    removeExpireAd()
    if (realAdList.isNotEmpty()) return
    if (isLoading) return
    isLoading = true
    doLoadAd(context)
}

private fun AdWithFullScreenState.doLoadAd(context: Context) {

    fun realLoad(index: Int) {
        val configDetail = configDetails.getOrNull(index)
        if (configDetail == null) {
            onLoaded?.invoke(false)
            isLoading = false
            return
        }

        val baseAd = when (configDetail.platform) {
            "admob" -> AdWithFullScreen(adLocation, configDetail)
            else -> null
        }

        if (baseAd == null) {
            realLoad(index + 1)
            return
        }

        baseAd.loadAd(context) { success, msg ->
            if (success) {
                realAdList.add(baseAd)
                realAdList.sortByDescending { it.getAdConfigDetail()?.weight ?: 0 }
                onLoaded?.invoke(true)
                isLoading = false
                LogUtils.d("AdWithFullScreenState.doLoadAd", "$adLocation -  ${configDetail.type} - ${configDetail.id} load success")
            } else {
                LogUtils.e("AdWithFullScreenState.doLoadAd", "$adLocation - ${configDetail.type} - ${configDetail.id} load failed: $msg")
                realLoad(index + 1)
            }
        }
    }

    realLoad(0)
}

fun AdWithFullScreenState.showFullScreenAd(activity: BaseActivity<*>, posId: String = adLocation, showDialog: Boolean = true, onClose: () -> Unit = {}) {

    if (realAdList.isEmpty()) {
        onClose.invoke()
        return
    }

    runCatching {
        activity.lifecycleScope.launch {
            val ad = realAdList.removeFirstOrNull()
            if (ad is AdWithFullScreen) {
                if (showDialog) {
                    val dialog = AdLoadingDialog()
                    dialog.show(activity.supportFragmentManager, "AdLoadingDialog")
                    delay(300)
                    dialog.dismiss()
                }
                ad.showAd(activity, null, onClose)

                //PostUtils.postCustomEvent("cm_ad_impression", hashMapOf("ad_pos_id" to posId))
            } else {
                onClose.invoke()
                return@launch
            }
            onLoaded = {}
            loadAd(activity)
        }
    }
}