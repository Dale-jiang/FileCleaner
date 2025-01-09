package com.clean.filecleaner.ui.ad

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ui.base.BaseActivity

fun AdWithNativeState.canShow(activity: BaseActivity<*>): Boolean {
    return realAdList.isNotEmpty() &&
            activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
}

fun AdWithNativeState.initAdConfigDetails(list: List<AdConfigDetails>?) {
    configDetails.run {
        clear()
        list?.run { addAll(this) }
        sortByDescending { it.weight }
    }
}

fun AdWithNativeState.removeExpireAd() {
    runCatching {
        realAdList.firstOrNull()?.takeIf { it.isAdExpire() }?.let {
            realAdList.remove(it)
        }
    }
}

fun AdWithNativeState.loadAd(context: Context = app) {
    if (configDetails.isEmpty()) return
    if (adManagerState.hasReachedUnusualAdLimit()) return
    removeExpireAd()
    if (realAdList.isNotEmpty()) return
    if (isLoading) return
    isLoading = true
    doLoadAd(context)
}


fun AdWithNativeState.showNativeAd(activity: BaseActivity<*>, parent: ViewGroup, posId: String = adLocation, callback: (IAd) -> Unit) {
    if (realAdList.isEmpty()) return
    val ad = realAdList.removeFirstOrNull() ?: return

    if (ad is AdWithNative) {
        ad.posId = posId
        ad.showAd(activity, parent)
        callback.invoke(ad)
    }
    onLoaded = {}
    loadAd(activity)
}

fun AdWithNativeState.waitAdLoading(context: Context, onLoad: (Boolean) -> Unit = {}) {
    if (realAdList.isNotEmpty()) {
        onLoad.invoke(true)
    } else {
        onLoaded = onLoad
        loadAd(context)
    }
}

private fun AdWithNativeState.doLoadAd(context: Context) {

    fun realLoad(index: Int) {

        val configDetails = configDetails.getOrNull(index)

        if (configDetails == null) {
            onLoaded?.invoke(false)
            isLoading = false
            return
        }

        val baseAd = when (configDetails.platform) {
            "admob" -> AdWithNative(adLocation, configDetails)
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
                LogUtils.d("AdWithNativeState.doLoadAd", "$adLocation - ${configDetails.type} - ${configDetails.id} load success")
            } else {
                LogUtils.e("AdWithNativeState.doLoadAd", "$adLocation - ${configDetails.type} - ${configDetails.id} load failed: $msg")
                realLoad(index + 1)
            }
        }
    }
    realLoad(0)
}