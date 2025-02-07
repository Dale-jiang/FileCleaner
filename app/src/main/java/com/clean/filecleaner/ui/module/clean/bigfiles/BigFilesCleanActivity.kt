package com.clean.filecleaner.ui.module.clean.bigfiles

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.ActivityBigFilesCleanBinding
import com.clean.filecleaner.databinding.LayoutBigFilesPopBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.opFile
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
import com.clean.filecleaner.ui.module.clean.bigfiles.viewmodel.BigFilesHelper
import com.clean.filecleaner.ui.module.clean.bigfiles.viewmodel.BigFilesViewModel
import com.clean.filecleaner.ui.module.clean.bigfiles.viewmodel.BigFilesViewModel.Companion.bigFiles
import com.clean.filecleaner.ui.module.dialog.AdLoadingDialog
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.ui.module.filemanager.FileInfo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BigFilesCleanActivity : BaseActivity<ActivityBigFilesCleanBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityBigFilesCleanBinding = ActivityBigFilesCleanBinding.inflate(layoutInflater)

    private var adapter: BigFilesAdapter? = null
    private var timeTag = 0L
    private var currentPopupWindow: PopupWindow? = null
    private var typeFilter = -1

    private val viewModel by viewModels<BigFilesViewModel>()
    private var checkedList: MutableList<FileInfo> = mutableListOf()

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

    @SuppressLint("NotifyDataSetChanged")
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
            CommonDialog(
                title = getString(R.string.warning),
                message = getString(R.string.do_you_wish_to_delete_this),
                rightBtn = getString(R.string.delete),
                leftBtn = getString(R.string.cancel),
                cancelable = true,
                rightClick = {
                    val dialog = AdLoadingDialog("${getString(R.string.deleting)}...")
                    dialog.show(supportFragmentManager, "LoadingDialog")
                    deleteFiles {
                        dialog.dismiss()
                        setDeleteButton()
                        handleListData()
                    }
                }
            ).show(supportFragmentManager, "CommonDialog")
        }


        viewModel.onCompleted.observe(this) {
            lifecycleScope.launch(Dispatchers.IO) {

                val finalList = filterList(bigFiles)

                val delayTime = timeTag + 2000 - System.currentTimeMillis()
                if (delayTime > 0) delay(delayTime)

                withContext(Dispatchers.Main) {
                    fullScreenAdShow {

                        stopLoadingAnim()
                        if (finalList.isEmpty()) {
                            TransitionManager.beginDelayedTransition(binding.root)
                        }

                        adapter?.list?.clear()
                        adapter?.list?.addAll(finalList)
                        adapter?.notifyDataSetChanged()

                        binding.loadingView.isVisible = false
                        binding.bottomView.isVisible = finalList.isNotEmpty()
                        binding.emptyView.isVisible = finalList.isEmpty()

                    }
                }

            }

        }

    }

    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this@BigFilesCleanActivity)
        setListener()
        setUpAdapter()
        setDeleteButton()
        timeTag = System.currentTimeMillis()
        viewModel.queryFiles(this@BigFilesCleanActivity)

        with(binding) {
            toolbar.title.text = getString(R.string.big_file)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun handleListData() = runCatching {
        bigFiles.onEach { it.isSelected = false }
        checkedList.clear()
        val list = filterList(bigFiles)
        adapter?.list?.clear()
        adapter?.list?.addAll(list)
        adapter?.notifyDataSetChanged()
        setDeleteButton()
        binding.bottomView.isVisible = list.isNotEmpty()
        binding.emptyView.isVisible = list.isEmpty()
    }

    private fun setDeleteButton() {
        binding.btnClean.isEnabled = checkedList.isNotEmpty()
        binding.btnClean.alpha = if (checkedList.isEmpty()) 0.3f else 1.0f
        if (checkedList.isNotEmpty()) {
            binding.btnClean.text = getString(R.string.delete_size, Formatter.formatFileSize(this, checkedList.sumOf { it.size }))

        } else binding.btnClean.text = getString(R.string.delete)
    }

    private fun filterList(list: MutableList<FileInfo>): MutableList<FileInfo> {

        val currentTime = System.currentTimeMillis()

        val filteredList = list.filter { fileInfo ->

            val isTypeMatch = when (typeList.indexOfFirst { it.select }) {
                0 -> true
                1 -> fileInfo.filetype == FileTypes.TYPE_IMAGE
                2 -> fileInfo.filetype == FileTypes.TYPE_VIDEO
                3 -> fileInfo.filetype == FileTypes.TYPE_AUDIO
                4 -> BigFilesHelper.isDocument(fileInfo.filetype!!)
                5 -> fileInfo.filetype == FileTypes.TYPE_ARCHIVES
                6 -> fileInfo.filetype == FileTypes.TYPE_APK
                else -> fileInfo.filetype == FileTypes.TYPE_OTHER
            }

            val isSizeMatch = when (sizeList.indexOfFirst { it.select }) {
                1 -> fileInfo.size >= 20_000_000
                2 -> fileInfo.size >= 50_000_000
                3 -> fileInfo.size >= 100_000_000
                4 -> fileInfo.size >= 500_000_000
                else -> true
            }

            val isTimeMatch = when (timeList.indexOfFirst { it.select }) {
                1 -> currentTime - fileInfo.addTime >= 604800000 // 7 days
                2 -> currentTime - fileInfo.addTime >= 1814400000 // 21 days
                3 -> currentTime - fileInfo.addTime >= 7257600000L // 3 months
                4 -> currentTime - fileInfo.addTime >= 14515200000L // 6 months
                5 -> currentTime - fileInfo.addTime >= 31449600000L // 1 year
                else -> true
            }

            isTypeMatch && isSizeMatch && isTimeMatch
        }

        return filteredList.sortedByDescending { it.size }.toMutableList()
    }


    private fun deleteFiles(onCompleted: Callback) {

        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            LogUtils.e("Error: ${throwable.message}")
        }

        lifecycleScope.launch(Dispatchers.IO + errorHandler) {
            checkedList.forEach { File(it.path).deleteRecursively() }
            bigFiles.removeAll(checkedList)
            checkedList.clear()
            delay(600L)
            withContext(Dispatchers.Main) {
                onCompleted.invoke()
            }
        }
    }

    private fun setUpAdapter() {

        with(binding) {
            adapter = BigFilesAdapter(this@BigFilesCleanActivity, list = mutableListOf(), clickListener = {
                opFile(it.path, it.mimetype)
            }, checkboxListener = { item, isChecked ->
                if (isChecked) checkedList.add(item) else checkedList.remove(item)
                setDeleteButton()
            })

            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
//            val controller = AnimationUtils.loadLayoutAnimation(this@BigFilesCleanActivity, R.anim.recyclerview_animation_controller)
//            recyclerView.layoutAnimation = controller
//            recyclerView.scheduleLayoutAnimation()
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
            handleListData()
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

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
        bigFiles.clear()
    }

}