package com.clean.filecleaner.ui.module.clean.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.AppUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import com.clean.filecleaner.databinding.ActivityApplicationManagementBinding
import com.clean.filecleaner.ext.getApplicationIconDrawable
import com.clean.filecleaner.ext.getApplicationLabelString
import com.clean.filecleaner.ext.hasUsagePermissions
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
import com.clean.filecleaner.utils.AppLifeHelper.jumpToSettings
import com.clean.filecleaner.utils.Tools.getAppDataSize
import com.clean.filecleaner.utils.Tools.isSystemApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApplicationManagementActivity : BaseActivity<ActivityApplicationManagementBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityApplicationManagementBinding = ActivityApplicationManagementBinding.inflate(layoutInflater)
    private var adapter: ApplicationManagementAdapter? = null
    private var timeTag = 0L

    private val uninstallLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        jumpToSettings = false
        updateAdapter()
    }

    private fun setBackListener() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@ApplicationManagementActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this@ApplicationManagementActivity)

        setBackListener()
        getInstalledApps()

        with(binding) {
            toolbar.title.text = getString(R.string.app_manager)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

    }

    private fun setUpAdapter(finalList: MutableList<ApplicationInfo>) {

        with(binding) {
            adapter = ApplicationManagementAdapter(this@ApplicationManagementActivity, list = finalList) {
                kotlin.runCatching {
                    jumpToSettings = true
                    uninstallLauncher.launch(Intent(Intent.ACTION_DELETE, Uri.parse("package:${it.pkgName}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        putExtra(Intent.EXTRA_RETURN_RESULT, true)
                    })
                }
            }

            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@ApplicationManagementActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateAdapter() {
        adapter?.apply {
            val dataList = list.filter { AppUtils.isAppInstalled(it.pkgName) }
            list.clear()
            list.addAll(dataList)
            notifyDataSetChanged()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledApps() {
        timeTag = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            val hasUsagePerm = hasUsagePermissions()
            val installedPackages = app.packageManager.getInstalledPackages(0)

            val itemList = installedPackages.filter { packageInfo ->
                val packageName = packageInfo.packageName
                packageName.isNotBlank() && !isSystemApp(packageName) && packageName != app.packageName
            }.mapNotNull { packageInfo ->
                val packageName = packageInfo.packageName
                runCatching {
                    ApplicationInfo(
                        drawable = getApplicationIconDrawable(packageName),
                        pkgName = packageName,
                        appName = getApplicationLabelString(packageName),
                        installTime = packageInfo.firstInstallTime,
//                        lastUsedTime = getLastUsedTime(packageInfo.firstInstallTime, packageName),
                        usedSize = if (hasUsagePerm) getAppDataSize(packageName) else 0L
                    )
                }.getOrNull()
            }

            val finalList = if (hasUsagePerm) {
                itemList.sortedByDescending { it.usedSize }
            } else {
                itemList
            }

            val delayTime = timeTag + 2000 - System.currentTimeMillis()
            if (delayTime > 0) delay(delayTime)

            withContext(Dispatchers.Main) {
                fullScreenAdShow {
                    binding.loadingView.isVisible = false
                    setUpAdapter(finalList.toMutableList())
                }
            }
        }
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
                delay(200L)
            }
            adState.showFullScreenAd(this@ApplicationManagementActivity, "fc_scan_int") { onComplete() }
        }
    }


    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

}