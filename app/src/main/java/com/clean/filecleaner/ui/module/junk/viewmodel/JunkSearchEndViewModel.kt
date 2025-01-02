package com.clean.filecleaner.ui.module.junk.viewmodel

import android.annotation.SuppressLint
import android.app.usage.StorageStatsManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPStaticUtils
import com.clean.filecleaner.data.LAST_CLEAN_CACHE_TIME
import com.clean.filecleaner.data.app
import com.clean.filecleaner.ext.calculateDirectorySizeSafely
import com.clean.filecleaner.ext.getApplicationIconDrawable
import com.clean.filecleaner.ext.getApplicationLabelString
import com.clean.filecleaner.ext.getFileSize
import com.clean.filecleaner.ext.hasUsagePermissions
import com.clean.filecleaner.ext.isGrantAndroidData
import com.clean.filecleaner.ui.module.junk.bean.TrashItemCache
import com.clean.filecleaner.utils.AndroidVersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class JunkSearchEndViewModel : ViewModel() {

    private val storageStats by lazy {
        if (AndroidVersionUtils.isAndroid8OrAbove()) {
            app.getSystemService(Context.STORAGE_STATS_SERVICE) as? StorageStatsManager
        } else null
    }

    val showCacheLoadingObserver = MutableLiveData<Boolean>()
    val resetCacheViewObserver = MutableLiveData<Boolean>()
    val appCacheDataObserver = MutableLiveData<MutableList<TrashItemCache>>()


    @SuppressLint("QueryPermissionsNeeded")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun doAtLeastAndroid12(): Result<MutableList<TrashItemCache>> = runCatching {
        // 权限判断
        if (!app.hasUsagePermissions()) {
            resetCacheViewObserver.postValue(true)
            return@runCatching mutableListOf()
        }
        // 例如距离上次清理不足 3 分钟就不再扫描
        if (System.currentTimeMillis() - SPStaticUtils.getLong(LAST_CLEAN_CACHE_TIME) < 4 * 60 * 1000) {
            resetCacheViewObserver.postValue(false)
            return@runCatching mutableListOf()
        }

        val itemList = mutableListOf<TrashItemCache>()

        // 遍历所有已安装的应用，查询 externalCacheBytes
        app.packageManager.getInstalledPackages(0).forEach { packageInfo ->
            val pkgName = packageInfo.packageName
            if (pkgName.isNotBlank() && pkgName != app.packageName) {
                val externalCacheBytes = getCacheSize(pkgName)
                if (externalCacheBytes > 0L) {
                    itemList.add(
                        TrashItemCache(
                            name = app.getApplicationLabelString(pkgName),
                            fileSize = externalCacheBytes,
                            pkgName = pkgName,
                            drawable = app.getApplicationIconDrawable(pkgName)
                        )
                    )
                }
            }
        }
        // 大小从大到小排序
        itemList.sortByDescending { it.fileSize }
        itemList
    }


    private fun doAtLeastAndroid11(): Result<MutableList<TrashItemCache>> = runCatching {
        if (!app.isGrantAndroidData()) {
            resetCacheViewObserver.postValue(true)
            return@runCatching mutableListOf()
        }

        val documentFile = DocumentFile.fromTreeUri(
            app, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
        ) ?: run {
            resetCacheViewObserver.postValue(false)
            return@runCatching mutableListOf()
        }

        val fileList = documentFile.listFiles().toMutableList()
        if (fileList.isEmpty()) {
            resetCacheViewObserver.postValue(false)
            return@runCatching mutableListOf()
        }

        val itemList = mutableListOf<TrashItemCache>()
        fileList.forEach { docFile ->
            val pkgName = docFile.name
            if (!pkgName.isNullOrBlank() && pkgName != app.packageName) {
                val cacheFile = docFile.findFile("cache")
                if (cacheFile != null && cacheFile.exists() && AppUtils.isAppInstalled(pkgName)) {
                    val cacheSize = cacheFile.calculateDirectorySizeSafely()
                    if (cacheSize > 0) {
                        val label = app.getApplicationLabelString(pkgName)
                        if (label.isNotBlank()) {
                            itemList.add(
                                TrashItemCache(
                                    name = label,
                                    path = cacheFile.uri.toString(),
                                    fileSize = cacheSize,
                                    pkgName = pkgName,
                                    drawable = app.getApplicationIconDrawable(pkgName)
                                )
                            )
                        }
                    }
                }
            }
        }
        itemList.sortByDescending { it.fileSize }
        itemList
    }

    //region Android 11 以下
    @SuppressLint("QueryPermissionsNeeded")
    private fun doUnderAndroid11(): Result<MutableList<TrashItemCache>> = runCatching {
        // 检查外部存储是否就绪
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            resetCacheViewObserver.postValue(false)
            return@runCatching mutableListOf()
        }

        // /Android/data
        val androidDataFile = File(Environment.getExternalStorageDirectory(), "Android/data")
        // 拼接到 /cache 目录
        val formatter = "${androidDataFile.absolutePath}/%s/cache"

        val installedPackages = app.packageManager.getInstalledPackages(0)
        val itemList = installedPackages
            .mapNotNull { packageInfo ->
                val pkgName = packageInfo.packageName
                if (pkgName.isBlank() || pkgName == app.packageName) {
                    return@mapNotNull null
                }
                val cachePath = String.format(formatter, pkgName)
                val externalCacheBytes = File(cachePath).getFileSize()
                if (externalCacheBytes > 0L) {
                    TrashItemCache(
                        name = app.getApplicationLabelString(pkgName),
                        path = cachePath,
                        fileSize = externalCacheBytes,
                        pkgName = pkgName,
                        drawable = app.getApplicationIconDrawable(pkgName)
                    )
                } else null
            }
            .toMutableList()

        itemList.sortByDescending { it.fileSize }
        itemList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCacheSize(packageName: String): Long {
        val stats = storageStats?.queryStatsForPackage(
            StorageManager.UUID_DEFAULT,
            packageName,
            Process.myUserHandle()
        ) ?: return 0L
        return if (AndroidVersionUtils.isAndroid12OrAbove()) {
            // Android 12+ 特有字段
            stats.externalCacheBytes
        } else {
            // Android 8-11 的处理逻辑（此处仅示例，实际要根据需求判断）
            stats.cacheBytes
        }
    }


    fun getAppCaches() {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            showCacheLoadingObserver.postValue(true)

            val result = when {
                AndroidVersionUtils.isAndroid12OrAbove() -> doAtLeastAndroid12()
                AndroidVersionUtils.isAndroid11OrAbove() -> doAtLeastAndroid11()
                else -> doUnderAndroid11()
            }

            result
                .onSuccess { itemList ->
                    appCacheDataObserver.postValue(itemList)
                }
                .onFailure {
                    resetCacheViewObserver.postValue(false)
                }
        }
    }

}