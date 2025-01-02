package com.clean.filecleaner.ext

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.clean.filecleaner.data.app
import com.clean.filecleaner.data.storagePermissions
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid10OrAbove
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid11OrAbove
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid12OrAbove

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
