package com.clean.filecleaner.ext

import android.graphics.Color
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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