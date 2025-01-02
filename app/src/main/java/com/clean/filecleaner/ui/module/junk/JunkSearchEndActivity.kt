package com.clean.filecleaner.ui.module.junk

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
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.SPStaticUtils
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.LAST_CLEAN_CACHE_TIME
import com.clean.filecleaner.databinding.ActivityJunkSearchEndBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.module.AppCacheTipsActivity
import com.clean.filecleaner.ui.module.dialog.CachePermissionRequestDialog
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.ui.module.junk.adapter.JunkSearchEndAdapter
import com.clean.filecleaner.ui.module.junk.bean.JunkType
import com.clean.filecleaner.ui.module.junk.bean.TrashItemParent
import com.clean.filecleaner.ui.module.junk.viewmodel.JunkSearchEndViewModel
import com.clean.filecleaner.ui.module.junk.viewmodel.allJunkDataList
import com.clean.filecleaner.utils.AndroidVersionUtils
import com.clean.filecleaner.utils.AppLifeHelper.isFromSettings

class JunkSearchEndActivity : JunkSearchEndBaseActivity<ActivityJunkSearchEndBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.topLayout, binding.root)
    override fun inflateViewBinding(): ActivityJunkSearchEndBinding = ActivityJunkSearchEndBinding.inflate(layoutInflater)

    private val viewModel by viewModels<JunkSearchEndViewModel>()
    private lateinit var mAdapter: JunkSearchEndAdapter

    override fun folderLauncherEnd() {
        viewModel.getAppCaches()
    }

    override fun above12LauncherEnd() {
        SPStaticUtils.put(LAST_CLEAN_CACHE_TIME, System.currentTimeMillis())

        ToastUtils.showLong("clean....")

//        toActivity<CleanResultActivity> {
//            putExtra(CleanResultActivity.INTENT_KEY, getString(R.string.freed_up_of_space_for_you, binding.tvSize.text))
//        }
//        finish()
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
//                    toActivity<CleanResultActivity> {
//                        putExtra(CleanResultActivity.INTENT_KEY, getString(R.string.freed_up_of_space_for_you, binding.tvSize.text))
//                    }
//                    finish()
                ToastUtils.showLong("结果页")
            }
        }


    }

    private fun setupObServers() {

        with(viewModel) {

            appCacheDataObserver.observe(this@JunkSearchEndActivity) { itemList ->

                TransitionManager.beginDelayedTransition(binding.root)
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
                    ToastUtils.showLong("结果页")
                    // 如果后续有跳转到其他 Activity 的逻辑可在这里执行
                    // toActivity<CleanResultActivity>()
                    // finish()
                    return@observe
                }

                firstItem?.takeIf { it.trashType == JunkType.APP_CACHE }
                    ?.apply {
                        isLoading = false
                        TransitionManager.beginDelayedTransition(binding.root)
                        binding.btnClean.isVisible = true
                        mAdapter.notifyItemChanged(0)
                    }
            }

            showCacheLoadingObserver.observe(this@JunkSearchEndActivity) {
                val firstItem = allJunkDataList.firstOrNull() as? TrashItemParent
                if (firstItem?.trashType == JunkType.APP_CACHE) {
                    firstItem.isLoading = true
                    TransitionManager.beginDelayedTransition(binding.root)
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
            junkSize.text = formatFileSize(this@JunkSearchEndActivity, selectedItems.sumOf { it.fileSize })
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
                isFromSettings = true
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

}