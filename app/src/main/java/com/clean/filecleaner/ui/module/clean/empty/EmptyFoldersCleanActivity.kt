package com.clean.filecleaner.ui.module.clean.empty

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityEmptyFoldersCleanBinding
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EmptyFoldersCleanActivity : BaseActivity<ActivityEmptyFoldersCleanBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityEmptyFoldersCleanBinding = ActivityEmptyFoldersCleanBinding.inflate(layoutInflater)

    private val skipFolders = listOf("obb", "data")
    private val extraPath = "${Environment.getExternalStorageDirectory().absolutePath}${File.separator}Android"
    private var adapter: EmptyFoldersCleanAdapter? = null
    private var timeTag = 0L

    private fun setListener() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@EmptyFoldersCleanActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnClean.setOnClickListener {
            startActivity(Intent(this, EmptyFoldersCleanEndActivity::class.java).apply {
                putStringArrayListExtra("EMPTY_FOLDERS", ArrayList(adapter!!.list))
            })
            finish()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this@EmptyFoldersCleanActivity)

        setListener()
        getEmptyFolders()

        with(binding) {
            toolbar.title.text = getString(R.string.empty_folder)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

    }

    private fun setUpAdapter(finalList: List<String>) {

        with(binding) {
            adapter = EmptyFoldersCleanAdapter(this@EmptyFoldersCleanActivity, list = finalList)

            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@EmptyFoldersCleanActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }


    private fun getEmptyFolders() {
        timeTag = System.currentTimeMillis()
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {

            val allEmptyFoldersList = scanEmptyFolders()

            val delayTime = timeTag + 2000 - System.currentTimeMillis()
            if (delayTime > 0) delay(delayTime)

            withContext(Dispatchers.Main) {
                fullScreenAdShow {
                    stopLoadingAnim()
                    if (allEmptyFoldersList.isEmpty()) {
                        TransitionManager.beginDelayedTransition(binding.root)
                    }
                    binding.totalSize.text = "${allEmptyFoldersList.size}"
                    binding.loadingView.isVisible = false
                    binding.bottomView.isVisible = allEmptyFoldersList.isNotEmpty()
                    binding.emptyView.isVisible = allEmptyFoldersList.isEmpty()
                    setUpAdapter(allEmptyFoldersList)
                    nativeAdShow()
                }
            }
        }
    }


    private fun scanEmptyFolders(root: File = Environment.getExternalStorageDirectory()): List<String> {
        val emptyFoldersDataList = mutableListOf<String>()
        root.isEmptyFolder(emptyFoldersDataList)
        return emptyFoldersDataList
    }

    private fun File.isEmptyFolder(emptyFoldersDataList: MutableList<String>): Boolean {
        if (isSkipFolder() || !canWrite() || absolutePath == extraPath) {
            return false
        }

        if (!isDirectory) {
            return false
        }

//        val isEmpty = listFiles()?.all { file ->
//            if (file.isDirectory) {
//                file.isEmptyFolder(emptyFoldersDataList)
//            } else {
//                false
//            }
//        } ?: true

        var isEmpty = true
        val files = listFiles()
        files?.forEach { file ->
            isEmpty = if (file.isDirectory) {
                isEmpty and file.isEmptyFolder(emptyFoldersDataList)
            } else false
        }

        if (isEmpty) {
            emptyFoldersDataList.add(absolutePath)
        }

        return isEmpty
    }

    private fun File.isSkipFolder(): Boolean {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R &&
                skipFolders.any { absolutePath.contains(it, true) }
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
            adState.showFullScreenAd(this@EmptyFoldersCleanActivity, "fc_scan_int") { onComplete() }
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
                    adState.showNativeAd(this@EmptyFoldersCleanActivity, binding.adContainer, "fc_scan_nat") {
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