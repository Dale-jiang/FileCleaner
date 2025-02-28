package com.clean.filecleaner.ui.module.clean.screenshot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.Formatter
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityScreenshotCleanBinding
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
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.utils.AndroidVersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ScreenshotCleanActivity : BaseActivity<ActivityScreenshotCleanBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityScreenshotCleanBinding = ActivityScreenshotCleanBinding.inflate(layoutInflater)
    private var timeTag = 0L
    private var adapter: ScreenshotCleanGroupAdapter? = null
    private var mTotalSize = 0L

    companion object {
        val allScreenshotList = mutableListOf<ScreenshotCleanParent>()
    }

    private fun setBackListener() {

        binding.loadingView.setOnClickListener { }

        onBackPressedDispatcher.addCallback {
            kotlin.runCatching {
                allScreenshotList.clear()
            }
            startActivity(Intent(this@ScreenshotCleanActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.allChecked.setOnClickListener {
            selectAllBtnClick()
        }

        binding.btnClean.setOnClickListener {
            CommonDialog(
                title = getString(R.string.warning),
                message = getString(R.string.do_you_wish_to_delete_this),
                rightBtn = getString(R.string.delete),
                leftBtn = getString(R.string.cancel),
                cancelable = true,
                rightClick = {
                    startActivity(Intent(this, ScreenshotCleanEndActivity::class.java).apply {
                        if (mTotalSize > 0) {
                            putExtra("MESSAGE", getString(R.string.clean_end_tips, binding.totalSize.text))
                        }
                    })

                    finish()
                }
            ).show(supportFragmentManager, "CommonDialog")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this@ScreenshotCleanActivity)
        adManagerState.fcResultNatState.loadAd(this@ScreenshotCleanActivity)

        setBackListener()

        with(binding) {
            toolbar.title.text = getString(R.string.screenshot)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

        lifecycleScope.launch {
            fetchScreenshotList()
        }

    }

    private fun setUpAdapter() {
        with(binding) {
            adapter = ScreenshotCleanGroupAdapter(this@ScreenshotCleanActivity, list = allScreenshotList) {
                allChecked.isChecked = allScreenshotList.all { c -> c.isSelected }
                setUpCleanBtn()
            }
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@ScreenshotCleanActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }


    private fun setUpCleanBtn() {

        mTotalSize = allScreenshotList.sumOf { parent ->
            parent.children.sumOf { child -> if (child.isSelected) child.size else 0L }
        }

        val hasSelectedItems = mTotalSize > 0
        val isListEmpty = allScreenshotList.isEmpty()

        with(binding) {
            this.totalSize.text = Formatter.formatFileSize(this@ScreenshotCleanActivity, mTotalSize)
            btnClean.apply {
                isEnabled = hasSelectedItems
                alpha = if (hasSelectedItems) 1f else 0.3f
            }
            allChecked.apply {
                isEnabled = !isListEmpty
                if (!hasSelectedItems) isChecked = false
            }
            emptyView.isVisible = isListEmpty
            binding.bottomView.isVisible = !isListEmpty
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun selectAllBtnClick() {
        mTotalSize = 0
        allScreenshotList.forEach {
            val status = binding.allChecked.isChecked
            if (status) it.select() else it.deselect()
            it.children.forEach { c ->
                if (status) c.select() else c.deselect()
                if (c.isSelected) mTotalSize += c.size
            }
        }
        binding.totalSize.text = Formatter.formatFileSize(this@ScreenshotCleanActivity, mTotalSize)
        binding.btnClean.apply {
            isEnabled = mTotalSize > 0
            alpha = if (mTotalSize > 0) 1f else 0.3f
        }
        adapter?.notifyDataSetChanged()
    }

    private suspend fun fetchScreenshotList() = withContext(Dispatchers.IO + SupervisorJob()) {
        try {
            timeTag = System.currentTimeMillis()
            allScreenshotList.clear()

            val possibleScreenshotDirectories = mutableListOf(
                "Screenshot",
                "Screenshots",
                "ScreenCaptures",
                //"截屏"
            )
            if (AndroidVersionUtils.isAndroid10OrAbove()) {
                possibleScreenshotDirectories.add(Environment.DIRECTORY_SCREENSHOTS)
            }

            val projection = arrayOf(
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE
            )
            val imgList = mutableListOf<ScreenshotCleanSub>()

            val selection = possibleScreenshotDirectories.joinToString(" OR ") {
                "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            }
            val selectionArgs = possibleScreenshotDirectories.map { "%${it}/%" }.toTypedArray()

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->

                val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val displayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

                while (cursor.moveToNext()) {
                    val datetime = cursor.getLongOrNull(dateModifiedCol) ?: 0L
                    val path = cursor.getStringOrNull(dataCol).orEmpty()
                    val displayName = cursor.getStringOrNull(displayNameCol).orEmpty()
                    val size = cursor.getLongOrNull(sizeCol) ?: 0L

                    val file = File(path)
                    if (file.exists() && file.isFile && (path.endsWith(".png", ignoreCase = true) || path.endsWith(".jpg", ignoreCase = true))) {
                        imgList.add(ScreenshotCleanSub(displayName, size, datetime * 1000L, path))
                    }
                }
            }

            imgList.sortByDescending { it.time }
            imgList.groupBy { it.timeStr }.forEach { (_, list) ->
                allScreenshotList.add(ScreenshotCleanParent(children = list.toMutableList(), time = list.first().time))
            }

            val delayTime = timeTag + 2000 - System.currentTimeMillis()
            if (delayTime > 0) delay(delayTime)

            withContext(Dispatchers.Main) {
                fullScreenAdShow {
                    stopLoadingAnim()
                    if (allScreenshotList.isEmpty()) {
                        TransitionManager.beginDelayedTransition(binding.root)
                    }
                    binding.loadingView.isVisible = false
                    binding.bottomView.isVisible = allScreenshotList.isNotEmpty()
                    binding.emptyView.isVisible = allScreenshotList.isEmpty()
                    setUpAdapter()
                    setUpCleanBtn()
                    nativeAdShow()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            adState.showFullScreenAd(this@ScreenshotCleanActivity, "fc_scan_int") { onComplete() }
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
                    adState.showNativeAd(this@ScreenshotCleanActivity, binding.adContainer, "fc_scan_nat") {
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