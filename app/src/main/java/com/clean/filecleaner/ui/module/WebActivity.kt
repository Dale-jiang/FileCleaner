package com.clean.filecleaner.ui.module

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.WEB_URL
import com.clean.filecleaner.databinding.ActivityWebBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

class WebActivity : BaseActivity<ActivityWebBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityWebBinding = ActivityWebBinding.inflate(layoutInflater)

    private val mUrl by lazy { intent?.getStringExtra(WEB_URL) ?: "" }


    override fun initView(savedInstanceState: Bundle?) {

        if (mUrl.isEmpty()) {
            ToastUtils.showLong(getString(R.string.url_error))
            finish()
            return
        }

        onBackPressedDispatcher.addCallback {
            if (binding.webView.canGoBack()) binding.webView.goBack() else finish()
        }
        initWebView()
        binding.webView.loadUrl(mUrl)

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() = kotlin.runCatching {
        with(binding.webView) {
            settings.javaScriptEnabled = true
            webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    binding.toolbar.title.text = title ?: ""
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progress.isVisible = 100 != newProgress
                    binding.progress.progress = newProgress
                }
            }
            webViewClient = WebViewClient()
        }
    }

}

