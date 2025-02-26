package com.clean.filecleaner.ui.module.antivirus.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.getApplicationIconDrawable
import com.clean.filecleaner.ui.module.antivirus.VirusInfo
import com.clean.filecleaner.ui.module.antivirus.allVirusList
import com.trustlook.sdk.cloudscan.CloudScanClient
import com.trustlook.sdk.cloudscan.CloudScanListener
import com.trustlook.sdk.data.AppInfo
import com.trustlook.sdk.data.Region
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VirusScanViewModel : ViewModel() {

    private val installedPathPrefixes = listOf("/data/app/", "/system/app/", "/system/priv-app/", "/mnt/asec/")
    private var scanTimeoutJob: Job? = null

    val scanVirusNumChangedLiveData = MutableLiveData<Boolean>()
    val scanFailedLiveData = MutableLiveData<Int>()
    val scanCompletedLiveData = MutableLiveData<Boolean>()
    val scanPathLiveData = MutableLiveData<String>()

    private val virusScanClient by lazy { CloudScanClient.Builder(app).setRegion(Region.INTL).setConnectionTimeout(30000).setSocketTimeout(30000).build() }

    private val scanListener by lazy {
        object : CloudScanListener() {
            override fun onScanStarted() = Unit

            override fun onScanProgress(curr: Int, total: Int, appInfo: AppInfo?) {
                if (null == appInfo) return
                scanTimeoutJob?.cancel()
                scanPathLiveData.postValue(appInfo.apkPath)
                if (!appInfo.isSystemApp && appInfo.packageName.isNotBlank() && AppUtils.isAppInstalled(appInfo.packageName) && installedPathPrefixes.any {
                        appInfo.apkPath.startsWith(it)
                    }) {
                    if (appInfo.score > 5) {
                        allVirusList.add(
                            VirusInfo(
                                label = appInfo.appName,
                                path = appInfo.apkPath,
                                score = appInfo.score,
                                packageName = appInfo.packageName,
                                drawable = app.getApplicationIconDrawable(appInfo.packageName),
                                virusType = appInfo.virusName ?: "",
                                isApp = true
                            )
                        )
                        scanVirusNumChangedLiveData.postValue(true)
                    }
                } else {
                    if (appInfo.score > 5) {
                        allVirusList.add(
                            VirusInfo(
                                label = File(appInfo.apkPath).name,
                                path = appInfo.apkPath,
                                score = appInfo.score,
                                virusType = appInfo.virusName ?: "",
                                isApp = false
                            )
                        )
                        scanVirusNumChangedLiveData.postValue(true)
                    }
                }
            }

            override fun onScanError(errCode: Int, errMsg: String?) {
//                PostUtils.postCustomEvent("antivirus_scan_error", hashMapOf("code" to errCode))
                scanFailedLiveData.postValue(errCode)
            }

            override fun onScanCanceled() = Unit

            override fun onScanInterrupt() {
                scanFailedLiveData.postValue(1111)
            }

            override fun onScanFinished(result: MutableList<AppInfo>?) {
                if (scanFailedLiveData.value == null || scanFailedLiveData.value != 9) {
                    scanCompletedLiveData.postValue(true)
                }
            }
        }
    }


    fun startScanVirus() = runCatching {
        allVirusList.clear()
        virusScanClient?.startComprehensiveScan(300, scanListener)
        startTimeOutCount()
    }

    private fun startTimeOutCount() = runCatching {
        scanTimeoutJob?.cancel()
        scanTimeoutJob = viewModelScope.launch(Dispatchers.IO) {
            delay(35 * 1000L)
            withContext(Dispatchers.Main) {
                if (scanPathLiveData.value.isNullOrEmpty()) {
//                    PostUtils.postCustomEvent("antivirus_scan_error", hashMapOf("code" to 9))
                    scanFailedLiveData.postValue(9)
                }
            }
        }
    }

}