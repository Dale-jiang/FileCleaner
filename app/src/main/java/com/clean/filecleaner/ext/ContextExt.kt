package com.clean.filecleaner.ext

import android.app.AppOpsManager
import android.app.Application
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.storage.StorageManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.clean.filecleaner.data.app
import com.clean.filecleaner.data.storagePermissions
import com.clean.filecleaner.utils.AndroidVersionUtils
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid10OrAbove
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid11OrAbove
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid12OrAbove
import java.io.IOException

fun Application.canInteractive() = runCatching { (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive }.getOrNull() ?: false

fun Context.getApplicationLabelString(packageName: String) = runCatching {
    app.packageManager.getApplicationLabel(app.packageManager.getApplicationInfo(packageName, 0)).toString()
}.getOrNull() ?: ""

fun Context.getApplicationIconDrawable(packageName: String) = runCatching { app.packageManager.getApplicationIcon(packageName) }.getOrNull()

fun Context.hasAllStoragePermissions(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        storagePermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

fun Context.hasUsagePermissions(): Boolean {
    return try {
        val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        if (isAndroid10OrAbove()) {
            AppOpsManager.MODE_ALLOWED == appOpsManager.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, app.applicationInfo.uid, app.packageName)
        } else {
            AppOpsManager.MODE_ALLOWED == appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, app.applicationInfo.uid, app.packageName)
        }
    } catch (t: Throwable) {
        false
    }
}

fun Context.isGrantAppCache(): Boolean {
    return when {
        isAndroid12OrAbove() -> hasUsagePermissions()
        isAndroid11OrAbove() -> isGrantAndroidData()
        else -> true
    }

}

fun Context.isGrantAndroidData(): Boolean {
    return this.contentResolver.persistedUriPermissions.any { it.uri.toString() == "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata" }
}

fun Context.getStorageSizeInfo(): Pair<Long, Long> {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val storageStatsManager = getSystemService(StorageStatsManager::class.java)
            val totalBytes = storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            val freeBytes = storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
            Pair(totalBytes, totalBytes - freeBytes)
        } else {
            getStorageSizeByStatFs()
        }
    } catch (e: SecurityException) {
        getStorageSizeByStatFs()
    } catch (e: IOException) {
        getStorageSizeByStatFs()
    } catch (e: Throwable) {
        getStorageSizeByStatFs()
    }
}

private fun getStorageSizeByStatFs(): Pair<Long, Long> {
    val path = Environment.getDataDirectory()
    val statFs = StatFs(path.path)
    val blockSize = statFs.blockSizeLong
    val totalBlocks = statFs.blockCountLong
    val availableBlocks = statFs.availableBlocksLong

    val totalSize = totalBlocks * blockSize
    val usedSize = (totalBlocks - availableBlocks) * blockSize
    return Pair(totalSize, usedSize)
}

fun Context.getFirInstallTime(): Long {
    return runCatching { this.packageManager.getPackageInfo(this.packageName, 0).firstInstallTime }.getOrDefault(0L)
}

fun Context.getLastUpdateTime(): Long {
    return runCatching { this.packageManager.getPackageInfo(app.packageName, 0).lastUpdateTime }.getOrDefault(0L)
}


fun Context.isGrantedNotification() = run {
    if (AndroidVersionUtils.isAndroid13OrAbove()) {
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else NotificationManagerCompat.from(this).areNotificationsEnabled()
}

fun Context.isDarkMode() = run {
    try {
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    } catch (e: Exception) {
        false
    }
}