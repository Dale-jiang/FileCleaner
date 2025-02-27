package com.clean.filecleaner.ui.module.antivirus

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityVirusScanBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.antivirus.viewmodel.VirusScanViewModel
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VirusScanActivity : BaseActivity<ActivityVirusScanBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityVirusScanBinding = ActivityVirusScanBinding.inflate(layoutInflater)

    private val viewModel by viewModels<VirusScanViewModel>()

    private fun setListener() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@VirusScanActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        viewModel.apply {

            scanFailedLiveData.observe(this@VirusScanActivity) {

                // PostUtils.postCustomEvent("antivirus_scan_error_popshow")
                CommonDialog(
                    title = getString(R.string.scan_error),
                    message = getString(R.string.please_try_again),
                    rightBtn = getString(R.string.ok),
                    cancelable = false,
                    rightClick = { finish() }
                ).show(supportFragmentManager, "CommonDialog")

            }

            scanPathLiveData.observe(this@VirusScanActivity) {
                binding.tvPath.text = it
            }

            scanVirusNumChangedLiveData.observe(this@VirusScanActivity) {

                val tempList = ArrayList(allVirusList)

                val appNum = tempList.filter { it.isApp }.size
                val fileNum = tempList.filter { !it.isApp }.size

                binding.virusNum.text = "$fileNum"
                binding.malwareNum.text = "$appNum"

                if (fileNum > 0) {
                    binding.progress1.setIndicatorColor(ContextCompat.getColor(this@VirusScanActivity, R.color.color_red))
                    binding.virusNum.setTextColor(ContextCompat.getColor(this@VirusScanActivity, R.color.color_red))
                }

                if (appNum > 0) {
                    binding.progress2.setIndicatorColor(ContextCompat.getColor(this@VirusScanActivity, R.color.color_red))
                    binding.malwareNum.setTextColor(ContextCompat.getColor(this@VirusScanActivity, R.color.color_red))
                }

            }

            scanCompletedLiveData.observe(this@VirusScanActivity) {
                lifecycleScope.launch(Dispatchers.Main) {
                    stopLoadingAnim()
                    binding.progress1.isIndeterminate = false
                    binding.progress2.isIndeterminate = false
                    binding.progress1.progress = 100
                    binding.progress2.progress = 100
                    binding.tvLoading.text = getString(R.string.finished)
                    delay(800)

                    if (allVirusList.isEmpty()) {
//                        PostUtils.postCustomEvent("antivirus_res_page", hashMapOf("res" to "no"))

                        startActivity(Intent(this@VirusScanActivity, AntivirusEndActivity::class.java))
                        finish()
                    } else {
//                        PostUtils.postCustomEvent("antivirus_res_page", hashMapOf("res" to "yes"))
                        startActivity(Intent(this@VirusScanActivity, VirusListActivity::class.java))
                        finish()
                    }

                }
            }


        }

    }

    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this@VirusScanActivity)
        adManagerState.fcResultIntState.loadAd(this@VirusScanActivity)
        adManagerState.fcResultNatState.loadAd(this@VirusScanActivity)

        setListener()
        viewModel.startScanVirus()

        with(binding) {

            toolbar.title.text = getString(R.string.antivirus)
            progress1.isIndeterminate = true
            progress2.isIndeterminate = true
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

    }


    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }
}