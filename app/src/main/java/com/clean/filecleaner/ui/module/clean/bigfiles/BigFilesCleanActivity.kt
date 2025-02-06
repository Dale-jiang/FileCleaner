package com.clean.filecleaner.ui.module.clean.bigfiles

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityBigFilesCleanBinding
import com.clean.filecleaner.databinding.LayoutBigFilesPopBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.IAd
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.loadAd
import com.clean.filecleaner.ui.ad.showFullScreenAd
import com.clean.filecleaner.ui.ad.showNativeAd
import com.clean.filecleaner.ui.ad.waitAdLoading
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.clean.empty.EmptyFoldersCleanAdapter
import com.clean.filecleaner.ui.module.clean.empty.EmptyFoldersCleanEndActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BigFilesCleanActivity : BaseActivity<ActivityBigFilesCleanBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityBigFilesCleanBinding = ActivityBigFilesCleanBinding.inflate(layoutInflater)

    private var adapter: EmptyFoldersCleanAdapter? = null
    private var timeTag = 0L
    private var currentPopupWindow: PopupWindow? = null
    private var typeFilter = -1

    private val typeList by lazy {
        mutableListOf(
            BigFileSelection(getString(R.string.all_types), true),
            BigFileSelection(getString(R.string.big_file_photo), false),
            BigFileSelection(getString(R.string.big_file_video), false),
            BigFileSelection(getString(R.string.big_file_audio), false),
            BigFileSelection(getString(R.string.big_file_docs), false),
            BigFileSelection(getString(R.string.big_file_zip), false),
            BigFileSelection(getString(R.string.big_file_apk), false),
            BigFileSelection(getString(R.string.big_file_others), false),
        )
    }
    private val sizeList by lazy {
        mutableListOf(
            BigFileSelection("10 MB", true),
            BigFileSelection("20 MB", false),
            BigFileSelection("50 MB", false),
            BigFileSelection("100 MB", false),
            BigFileSelection("500 MB", false)
        )
    }

    private val timeList by lazy {
        mutableListOf(
            BigFileSelection(getString(R.string.all_time), true),
            BigFileSelection(getString(R.string.big_file_one_week), false),
            BigFileSelection(getString(R.string.big_file_one_month), false),
            BigFileSelection(getString(R.string.big_file_three_month), false),
            BigFileSelection(getString(R.string.big_file_six_month), false),
            BigFileSelection(getString(R.string.big_file_one_year), false)
        )
    }

    private fun setListener() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@BigFilesCleanActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.filterType.setOnClickListener {
            binding.ivType.animate().rotation(180.0f).setDuration(280L).start()
            if (typeFilter == 0) {
                currentPopupWindow?.dismiss()
                typeFilter = -1
            } else {
                showPopWindow(0)
                currentPopupWindow?.setOnDismissListener {
                    typeFilter = -1
                    currentPopupWindow = null
                    binding.ivType.animate().rotation(0.0f).setDuration(280L).start()
                }
            }

        }

        binding.filterSize.setOnClickListener {
            binding.ivSize.animate().rotation(180.0f).setDuration(280L).start()
            if (typeFilter == 1) {
                currentPopupWindow?.dismiss()
                typeFilter = -1
            } else {
                showPopWindow(1)
                currentPopupWindow?.setOnDismissListener {
                    typeFilter = -1
                    currentPopupWindow = null
                    binding.ivSize.animate().rotation(0.0f).setDuration(280L).start()
                }
            }
        }

        binding.filterTime.setOnClickListener {
            binding.ivTime.animate().rotation(180.0f).setDuration(280L).start()
            if (typeFilter == 2) {
                currentPopupWindow?.dismiss()
                typeFilter = -1
            } else {
                showPopWindow(2)
                currentPopupWindow?.setOnDismissListener {
                    typeFilter = -1
                    currentPopupWindow = null
                    binding.ivTime.animate().rotation(0.0f).setDuration(280L).start()
                }
            }
        }


        binding.btnClean.setOnClickListener {
            startActivity(Intent(this, EmptyFoldersCleanEndActivity::class.java).apply {
                putStringArrayListExtra("EMPTY_FOLDERS", ArrayList(adapter!!.list))
            })
            finish()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this@BigFilesCleanActivity)

        setListener()
        getBigFiles()

        with(binding) {
            toolbar.title.text = getString(R.string.big_file)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

    }

    private fun setUpAdapter(finalList: List<String>) {

        with(binding) {
            adapter = EmptyFoldersCleanAdapter(this@BigFilesCleanActivity, list = finalList)

            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@BigFilesCleanActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }

    private fun showPopWindow(type: Int) {
        typeFilter = type
        val popBinding = LayoutBigFilesPopBinding.inflate(layoutInflater)

        with(popBinding) {
            setupPopupWindow(root).also { currentPopupWindow = it }
            setupFilterAdapter(type).let { recyclerView.adapter = it }

            recyclerView.itemAnimator = null
            startSlideAnimation(recyclerView)

            currentPopupWindow?.showAsDropDown(binding.filterBar, 0, 0)
        }
    }

    private fun setupPopupWindow(contentView: View): PopupWindow {
        return PopupWindow(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isFocusable = false
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun setupFilterAdapter(type: Int): BigFileSelectionAdapter {
        val dataList = when (type) {
            0 -> typeList
            1 -> sizeList
            2 -> timeList
            else -> emptyList()
        }

        return BigFileSelectionAdapter(this@BigFilesCleanActivity) { selectedItem ->
            updateFilterView(type, selectedItem)
            //  viewModel.filterBigFiles()
            currentPopupWindow?.dismiss()
        }.apply {
            initData(dataList.toMutableList())
        }
    }

    private fun updateFilterView(type: Int, item: BigFileSelection) {
        val targetView = when (type) {
            0 -> binding.tvType
            1 -> binding.tvSize
            2 -> binding.tvTime
            else -> return
        }
        targetView.text = item.name
    }

    private fun startSlideAnimation(view: View) {
        val slideIn = AnimationUtils.loadAnimation(view.context, R.anim.slide_in_top)
        view.startAnimation(slideIn)
    }

    private fun getBigFiles() {
        timeTag = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {

            val allEmptyFoldersList = mutableListOf<String>()

            val delayTime = timeTag + 2000 - System.currentTimeMillis()
            if (delayTime > 0) delay(delayTime)

            withContext(Dispatchers.Main) {
                fullScreenAdShow {
                    stopLoadingAnim()
                    if (allEmptyFoldersList.isEmpty()) {
                        TransitionManager.beginDelayedTransition(binding.root)
                    }
                    binding.loadingView.isVisible = false
                    binding.bottomView.isVisible = allEmptyFoldersList.isNotEmpty()
                    binding.emptyView.isVisible = allEmptyFoldersList.isEmpty()
                    setUpAdapter(allEmptyFoldersList)
                    nativeAdShow()
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
                delay(210L)
            }
            adState.showFullScreenAd(this@BigFilesCleanActivity, "fc_scan_int") { onComplete() }
        }
    }


    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
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
                    adState.showNativeAd(this@BigFilesCleanActivity, binding.adContainer, "fc_scan_nat") {
                        ad = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ad?.destroy()
        stopLoadingAnim()
    }

}