package com.clean.filecleaner.ui.module.antivirus

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityAntivirusEndBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.IAd
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.showNativeAd
import com.clean.filecleaner.ui.ad.waitAdLoading
import com.clean.filecleaner.ui.base.StoragePermissionBaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.clean.bigfiles.BigFilesCleanActivity
import com.clean.filecleaner.ui.module.clean.duplicate.DuplicateFileCleanActivity
import com.clean.filecleaner.ui.module.clean.junk.JunkSearchActivity
import com.clean.filecleaner.ui.module.clean.screenshot.ScreenshotCleanActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AntivirusEndActivity : StoragePermissionBaseActivity<ActivityAntivirusEndBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityAntivirusEndBinding = ActivityAntivirusEndBinding.inflate(layoutInflater)

    private val resultMessage by lazy {
        intent?.getStringExtra("MESSAGE") ?: ""
    }

    private fun initBackListeners() {

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@AntivirusEndActivity, MainActivity::class.java))
            finish()
        }

        binding.clean.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@AntivirusEndActivity, JunkSearchActivity::class.java))
                finish()
            }
        }

        binding.bigFile.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@AntivirusEndActivity, BigFilesCleanActivity::class.java))
                finish()
            }
        }

        binding.duplicateFiles.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@AntivirusEndActivity, DuplicateFileCleanActivity::class.java))
                finish()
            }
        }

        binding.screenshot.setOnClickListener {
            requestPermissions {
                startActivity(Intent(this@AntivirusEndActivity, ScreenshotCleanActivity::class.java))
                finish()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.title.text = getString(R.string.antivirus)
             if (resultMessage.isNotEmpty()){
                 message.text = resultMessage
            }
            nativeAdShow()
        }
        initBackListeners()
    }

    private var ad: IAd? = null
    private fun nativeAdShow() {
        if (adManagerState.hasReachedUnusualAdLimit()) return
        DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_result_nat"))
        val adState = adManagerState.fcResultNatState
        adState.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(210L)
                if (adState.canShow()) {
                    ad?.destroy()
                    adState.showNativeAd(this@AntivirusEndActivity, binding.adContainer, "fc_result_nat") {
                        ad = it
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        ad?.destroy()
    }

}