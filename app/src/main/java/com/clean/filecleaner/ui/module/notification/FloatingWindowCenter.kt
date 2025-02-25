package com.clean.filecleaner.ui.module.notification

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.ui.module.SplashActivity
import com.clean.filecleaner.utils.AndroidVersionUtils.isAndroid8OrAbove

class FloatingWindowCenter(private val context: Context) {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var mImage: ImageView? = null
    private var closeButton: ImageView? = null
    private var contentTextView: TextView? = null
    private var buttonTextView: TextView? = null

    fun showFloatingWindow(notificationInfo: NotificationInfo) {
        LogUtils.e("----------000----->>>>")
        if (floatingView == null) {
            LogUtils.e("----------111----->>>>")
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            floatingView = LayoutInflater.from(context).inflate(R.layout.layout_floating_window, null)

            val params = WindowManager.LayoutParams(
                ScreenUtils.getScreenWidth() - SizeUtils.dp2px(10f), WindowManager.LayoutParams.WRAP_CONTENT,
                if (isAndroid8OrAbove()) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                x = 0
                y = SizeUtils.dp2px(25f)
            }

            LogUtils.e("---------222------>>>>${floatingView}")
            windowManager.addView(floatingView, params)

            closeButton = floatingView?.findViewById(R.id.close)
            contentTextView = floatingView?.findViewById(R.id.message)
            buttonTextView = floatingView?.findViewById(R.id.btn)
            mImage = floatingView?.findViewById(R.id.image)
        }

        LogUtils.e("----------333----->>>>")

        contentTextView?.text = context.getString(notificationInfo.messageId)
        buttonTextView?.text = context.getString(notificationInfo.btnStrId)
        mImage?.setImageResource(notificationInfo.icon)


        closeButton?.setOnClickListener {
            runCatching {
                hideFloatingWindow()
            }
        }

        floatingView?.setOnClickListener {
            launchOrAdjustActivity()
            hideFloatingWindow()
        }
    }


    private fun launchOrAdjustActivity() {
        runCatching {
            val intent = Intent(context, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    private fun hideFloatingWindow() {
        if (::windowManager.isInitialized && floatingView != null) {
            windowManager.removeView(floatingView)
            floatingView = null
        }
    }
}