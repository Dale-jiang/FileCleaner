package com.clean.filecleaner.ui.module.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.app
import com.clean.filecleaner.databinding.ActivityApplicationManagementBinding
import com.clean.filecleaner.ext.getApplicationIconDrawable
import com.clean.filecleaner.ext.getApplicationLabelString
import com.clean.filecleaner.ext.hasUsagePermissions
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.utils.Tools.getAppDataSize
import com.clean.filecleaner.utils.Tools.getLastUsedTime
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

    private fun setUpAdapter(finalList: List<ApplicationInfo>) {

        with(binding) {
            adapter = ApplicationManagementAdapter(this@ApplicationManagementActivity, list = finalList) {
                ToastUtils.showLong("ssss")
            }

            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@ApplicationManagementActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun getInstalledApps() {
        timeTag = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            val hasUsagePerm = hasUsagePermissions()
            val installedPackages = app.packageManager.getInstalledPackages(0)

            val itemList = installedPackages
                .filter { packageInfo ->
                    val packageName = packageInfo.packageName
                    packageName.isNotBlank() && !isSystemApp(packageName) && packageName != app.packageName
                }
                .mapNotNull { packageInfo ->
                    val packageName = packageInfo.packageName
                    runCatching {
                        ApplicationInfo(
                            drawable = getApplicationIconDrawable(packageName),
                            pkgName = packageName,
                            appName = getApplicationLabelString(packageName),
                            installTime = packageInfo.firstInstallTime,
                            lastUsedTime = getLastUsedTime(this@ApplicationManagementActivity, packageName),
                            usedSize = if (hasUsagePerm) getAppDataSize(packageName) else 0L
                        )
                    }.getOrNull()
                }

            val finalList = if (hasUsagePerm) {
                itemList.sortedByDescending { it.usedSize }
            } else {
                itemList
            }

            withContext(Dispatchers.Main) {

                val delayTime = timeTag + 2000 - System.currentTimeMillis()
                if (delayTime > 0) delay(delayTime)

                TransitionManager.beginDelayedTransition(binding.root)
                binding.loadingView.isVisible = false
                setUpAdapter(finalList)

            }
        }
    }


    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

}