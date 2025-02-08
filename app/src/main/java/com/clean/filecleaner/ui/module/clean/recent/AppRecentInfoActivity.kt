package com.clean.filecleaner.ui.module.clean.recent

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityAppRecentInfoBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.clean.recent.adapter.ViewPageAdapter
import com.clean.filecleaner.ui.module.clean.recent.fragment.LaunchesFragment
import com.clean.filecleaner.ui.module.filemanager.allFilesContainerList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppRecentInfoActivity : BaseActivity<ActivityAppRecentInfoBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityAppRecentInfoBinding = ActivityAppRecentInfoBinding.inflate(layoutInflater)

    private val fragmentList by lazy {
        mutableListOf<Fragment>(LaunchesFragment(), LaunchesFragment())
    }

    private var timeTag = 0L

    private fun setListeners() {
        onBackPressedDispatcher.addCallback {
            kotlin.runCatching {
                allFilesContainerList.clear()
            }
            startActivity(Intent(this@AppRecentInfoActivity, MainActivity::class.java))
            finish()
        }

        binding.loadingView.setOnClickListener { }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tabLaunches.setOnClickListener {
            binding.viewPager.currentItem = 0
            changeTab(0)
        }

        binding.tabScreenTime.setOnClickListener {
            binding.viewPager.currentItem = 1
            changeTab(1)
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                changeTab(position)
            }
        })

    }

    override fun initView(savedInstanceState: Bundle?) {
        setListeners()

        with(binding) {
            toolbar.title.text = getString(R.string.recent_app)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }

            viewPager.isUserInputEnabled = true
            viewPager.adapter = ViewPageAdapter(this@AppRecentInfoActivity, fragmentList)
            viewPager.offscreenPageLimit = 2

        }

        lifecycleScope.launch {
            delay(2000L)
            stopLoadingAnim()
            TransitionManager.beginDelayedTransition(binding.root)
            binding.loadingView.isVisible = false
        }
    }


    private fun changeTab(position: Int) {
        binding.apply {
            if (position == 0) {
                tabLaunches.setBackgroundResource(R.drawable.shape_bg_primary_8)
                tabLaunches.setTextColor(ContextCompat.getColor(this@AppRecentInfoActivity, R.color.white))

                tabScreenTime.setBackgroundColor(ContextCompat.getColor(this@AppRecentInfoActivity, R.color.trans))
                tabScreenTime.setTextColor(ContextCompat.getColor(this@AppRecentInfoActivity, R.color.color_9397a5))
            } else {
                tabLaunches.setBackgroundColor(ContextCompat.getColor(this@AppRecentInfoActivity, R.color.trans))
                tabLaunches.setTextColor(ContextCompat.getColor(this@AppRecentInfoActivity, R.color.color_9397a5))

                tabScreenTime.setBackgroundResource(R.drawable.shape_bg_primary_8)
                tabScreenTime.setTextColor(ContextCompat.getColor(this@AppRecentInfoActivity, R.color.white))
            }

        }
    }

    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    private fun fullScreenAdShow(onComplete: () -> Unit) = runCatching {
        if (adManagerState.hasReachedUnusualAdLimit()) return@runCatching onComplete()
        DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_scan_int"))
        val adState = adManagerState.fcBackScanIntState
        if (!adState.canShow()) {
            adState.loadAd(this)
            return@runCatching onComplete()
        }
        lifecycleScope.launch {
            while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                delay(210L)
            }
            adState.showFullScreenAd(this@AppRecentInfoActivity, "fc_scan_int") { onComplete() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }

}


