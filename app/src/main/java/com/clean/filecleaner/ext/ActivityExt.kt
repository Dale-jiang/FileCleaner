package com.clean.filecleaner.ext

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.BuildConfig
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import com.clean.filecleaner.utils.AppLifeHelper.jumpToSettings
import java.io.File

fun AppCompatActivity.immersiveMode(
    topView: ViewGroup? = null,
    bottomView: ViewGroup? = null,
    topPadding: Boolean = true,
    bottomPadding: Boolean = true,
    lightMode: Boolean = true
) {
    enableEdgeToEdge(
        statusBarStyle = if (lightMode) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT) else SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.light(Color.WHITE, Color.WHITE)
    )
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insetsCompat ->
        val barsInsets = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())

        if (topView != null) {
            topView.setPadding(0, if (topPadding) barsInsets.top else 0, 0, 0)
        } else {
            window.decorView.setPadding(0, if (topPadding) barsInsets.top else 0, 0, 0)
        }

        if (bottomView != null) {
            bottomView.setPadding(0, 0, 0, if (bottomPadding) barsInsets.bottom else 0)
        } else {
            window.decorView.setPadding(0, 0, 0, if (bottomPadding) barsInsets.bottom else 0)
        }

        insetsCompat
    }
}

fun Activity.opFile(path: String) = runCatching {
    val file = File(path)

    if (!file.exists() || file.isDirectory) {
        ToastUtils.showLong(getString(R.string.file_not_exist))
        return@runCatching
    }

    val fileUri = FileProvider.getUriForFile(
        app,
        "${BuildConfig.APPLICATION_ID}.FileCleanProvider",
        file
    )
    val extension = MimeTypeMap.getFileExtensionFromUrl(file.toString())
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

    if (mimeType == "application/vnd.android.package-archive" || file.name.endsWith(".apk")) {
        ToastUtils.showLong(getString(R.string.no_permission_to_install))
    } else {
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.also { intent ->
            jumpToSettings = true
            startActivity(intent)
        }
    }
}.onFailure {
    ToastUtils.showLong(getString(R.string.unknown_error))
}
