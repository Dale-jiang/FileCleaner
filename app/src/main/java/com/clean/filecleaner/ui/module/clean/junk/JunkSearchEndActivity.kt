package com.clean.filecleaner.ui.module.clean.junk

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.storage.StorageManager
import android.text.format.Formatter.formatFileSize
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityJunkSearchEndBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.IAd
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showNativeAd
import com.clean.filecleaner.ui.ad.waitAdLoading
import com.clean.filecleaner.ui.module.AppCacheTipsActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.clean.junk.adapter.JunkSearchEndAdapter
import com.clean.filecleaner.ui.module.clean.junk.bean.JunkType
import com.clean.filecleaner.ui.module.clean.junk.bean.TrashItemParent
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.JunkSearchEndViewModel
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.allJunkDataList
import com.clean.filecleaner.ui.module.dialog.CachePermissionRequestDialog
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.utils.AndroidVersionUtils
import com.clean.filecleaner.utils.AppLifeHelper.jumpToSettings
import com.clean.filecleaner.utils.AppPreferences.lastCleanCacheTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JunkSearchEndActivity : JunkSearchEndBaseActivity<ActivityJunkSearchEndBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.topLayout, binding.root)
    override fun inflateViewBinding(): ActivityJunkSearchEndBinding = ActivityJunkSearchEndBinding.inflate(layoutInflater)

    private val viewModel by viewModels<JunkSearchEndViewModel>()
    private lateinit var mAdapter: JunkSearchEndAdapter
    private var selectJunkSize = 0L

    override fun folderLauncherEnd() {
        viewModel.getAppCaches()
    }

    override fun above12LauncherEnd() {
        lastCleanCacheTime = System.currentTimeMillis()
        startActivity(Intent(this, JunkCleanEndActivity::class.java).apply {
//            if (selectJunkSize > 0) {
            putExtra("MESSAGE", getString(R.string.clean_end_tips, binding.junkSize.text))
//            }
        })
        finish()
    }


    private fun initBackListeners() {
        onBackPressedDispatcher.addCallback {
            CommonDialog(
                title = getString(R.string.app_name),
                message = getString(R.string.junk_exit_tips),
                leftBtn = getString(R.string.exit),
                rightBtn = getString(R.string.cancel),
                cancelable = true,
                leftClick = {
                    allJunkDataList.clear()
                    startActivity(Intent(this@JunkSearchEndActivity, MainActivity::class.java))
                    finish()
                }
            ).show(
                supportFragmentManager,
                "CommonDialog"
            )
        }
        binding.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultNatState.loadAd(this)
        nativeAdShow()

        setUpAdapter()
        initBackListeners()

        setSelectSize()
        viewModel.getAppCaches()

        setupObServers()

        binding.btnClean.setOnClickListener {
            val cacheParent = allJunkDataList.firstOrNull()
            if (AndroidVersionUtils.isAndroid12OrAbove() && cacheParent != null && cacheParent.trashType == JunkType.APP_CACHE && cacheParent.select) {
                above12Launcher.launch(Intent(StorageManager.ACTION_CLEAR_APP_CACHE))
            } else {
                startActivity(Intent(this, JunkCleanEndActivity::class.java).apply {
//                    if (selectJunkSize > 0) {
                    putExtra("MESSAGE", getString(R.string.clean_end_tips, binding.junkSize.text))
//                    }
                })

                finish()
            }
        }


    }

    private fun setupObServers() {

        with(viewModel) {

            appCacheDataObserver.observe(this@JunkSearchEndActivity) { itemList ->

                // TransitionManager.beginDelayedTransition(binding.root)
                binding.btnClean.isVisible = true

                val firstItem = allJunkDataList.firstOrNull() as? TrashItemParent
                if (itemList.isNotEmpty()) {
                    firstItem?.apply {
                        subItems.addAll(itemList)
                        fileSize = itemList.sumOf { it.fileSize }
                        isLoading = false
                        select = true
                    }
                    mAdapter.notifyItemChanged(0)
                    setSelectSize()
                } else {
                    resetCacheViewObserver.value = false
                }
            }


            resetCacheViewObserver.observe(this@JunkSearchEndActivity) { shouldReset ->

                val firstItem = allJunkDataList.firstOrNull() as? TrashItemParent

                if (!shouldReset && allJunkDataList.size == 1 && (firstItem?.subItems?.isEmpty() == true)) {
                    startActivity(Intent(this@JunkSearchEndActivity, JunkCleanEndActivity::class.java))
                    finish()
                    return@observe
                }

                firstItem?.takeIf { it.trashType == JunkType.APP_CACHE }
                    ?.apply {
                        isLoading = false
                        //    TransitionManager.beginDelayedTransition(binding.root)
                        binding.btnClean.isVisible = true
                        mAdapter.notifyItemChanged(0)
                    }
            }

            showCacheLoadingObserver.observe(this@JunkSearchEndActivity) {
                val firstItem = allJunkDataList.firstOrNull() as? TrashItemParent
                if (firstItem?.trashType == JunkType.APP_CACHE) {
                    firstItem.isLoading = true
                    // TransitionManager.beginDelayedTransition(binding.root)
                    binding.btnClean.isVisible = false
                    mAdapter.notifyItemChanged(0)
                }
            }

        }

    }

    private fun setUpAdapter() = runCatching {
        with(binding) {

            allJunkDataList.add(0, TrashItemParent(mutableListOf(), false, 0L, JunkType.APP_CACHE, false))
            mAdapter = JunkSearchEndAdapter(this@JunkSearchEndActivity, allJunkDataList,
                onItemChanged = {
                    setSelectSize()
                }, onButtonGrantCLick = {
                    CachePermissionRequestDialog { requestCachePermission() }.show(supportFragmentManager, "CachePermissionRequestDialog")
                })

            recyclerView.itemAnimator = null
            recyclerView.adapter = mAdapter
            val controller = AnimationUtils.loadLayoutAnimation(this@JunkSearchEndActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()

        }
    }

    private fun setSelectSize() = runCatching {
        val selectedItems = allJunkDataList
            .filterIsInstance<TrashItemParent>()
            .flatMap { it.subItems }
            .filter { it.select }

        binding.apply {
            btnClean.isEnabled = selectedItems.isNotEmpty()
            btnClean.alpha = if (selectedItems.isNotEmpty()) 1f else 0.3f
            selectJunkSize = selectedItems.sumOf { it.fileSize }
            junkSize.text = formatFileSize(this@JunkSearchEndActivity, selectJunkSize)
        }
    }


    private fun requestCachePermission() {
        when {
            AndroidVersionUtils.isAndroid12OrAbove() -> {
                // Android 12 及以上，直接请求权限
                requestPermissions {
                    viewModel.getAppCaches()
                }
            }

            else -> {
                // Android 12 以下，通过 OPEN_DOCUMENT_TREE 手动获取权限
                jumpToSettings = true
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET or
                                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    )
                    if (AndroidVersionUtils.isAndroid8OrAbove()) {
                        val fromTreeUri = DocumentFile.fromTreeUri(
                            this@JunkSearchEndActivity,
                            Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")
                        )
                        putExtra("android.provider.extra.INITIAL_URI", fromTreeUri?.uri)
                    }
                }
                folderLauncher.launch(intent)

                startActivity(Intent(this, AppCacheTipsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    private var ad: IAd? = null
    private fun nativeAdShow() {
        if (adManagerState.hasReachedUnusualAdLimit()) return
        DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_scan_nat"))
        val adState = adManagerState.fcScanNatState
        adState.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(210L)
                if (adState.canShow()) {
                    ad?.destroy()
                    adState.showNativeAd(this@JunkSearchEndActivity, binding.adContainer, "fc_scan_nat") {
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