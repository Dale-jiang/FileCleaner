package com.clean.filecleaner.ui.module.clean.duplicate

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.ActivityDuplicateFileCleanBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.opFile
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
import com.clean.filecleaner.ui.module.dialog.DuplicateFileCleanFilterDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class DuplicateFileCleanActivity : BaseActivity<ActivityDuplicateFileCleanBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityDuplicateFileCleanBinding = ActivityDuplicateFileCleanBinding.inflate(layoutInflater)
    private var timeTag = 0L
    private var keepType = 0

    private var adapter: DuplicateFileCleanGroupAdapter? = null

    companion object {
        val allDuplicateFileList = mutableListOf<DuplicateFileGroup>()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setListeners() {

        binding.loadingView.setOnClickListener { }

        onBackPressedDispatcher.addCallback {
            kotlin.runCatching {
                allDuplicateFileList.clear()
            }
            startActivity(Intent(this@DuplicateFileCleanActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.ivRight.setOnClickListener {
            DuplicateFileCleanFilterDialog(keepType) { type ->
                when (type) {
                    0 -> {
                        keepType = 0
                        adapter?.list?.forEach { p ->
                            p.children.forEach {
                                it.isSelected = !it.isLatest
                            }
                        }
                    }

                    1 -> {
                        keepType = 1
                        adapter?.list?.forEach { p ->
                            p.children.forEach {
                                it.isSelected = !it.isOldest
                            }
                        }
                    }

                    2 -> {
                        keepType = 2
                        adapter?.list?.forEach { p ->
                            p.children.forEach {
                                it.isSelected = false
                            }
                        }
                    }
                }
                adapter?.notifyDataSetChanged()
                setCleanBtn()
            }.show(supportFragmentManager, "DuplicateFileCleanFilterDialog")
        }

        binding.btnClean.setOnClickListener {
            CommonDialog(
                title = getString(R.string.warning),
                message = getString(R.string.do_you_wish_to_delete_this),
                rightBtn = getString(R.string.delete),
                leftBtn = getString(R.string.cancel),
                cancelable = true,
                rightClick = {
                    startActivity(Intent(this, DuplicateFileCleanEndActivity::class.java))
                    finish()
                }
            ).show(supportFragmentManager, "CommonDialog")
        }
    }


    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this@DuplicateFileCleanActivity)
        adManagerState.fcResultNatState.loadAd(this@DuplicateFileCleanActivity)

        setListeners()

        with(binding) {
            toolbar.title.text = getString(R.string.duplicate_files)
            toolbar.ivRight.setImageResource(R.drawable.svg_duplicate_filter)
            toolbar.ivRight.isInvisible = true
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

        fetchDuplicateFiles {
            lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
                val delayTime = timeTag + 2000 - System.currentTimeMillis()
                if (delayTime > 0) delay(delayTime)

                withContext(Dispatchers.Main) {
                    fullScreenAdShow {
                        if (allDuplicateFileList.isEmpty()) {
                            TransitionManager.beginDelayedTransition(binding.root)
                        }
                        binding.loadingView.isVisible = false
                        binding.toolbar.ivRight.isInvisible = allDuplicateFileList.isEmpty()
                        binding.emptyView.isVisible = allDuplicateFileList.isEmpty()
                        binding.bottomView.isVisible = allDuplicateFileList.isNotEmpty()
                        setUpAdapter()
                        setCleanBtn()
                        nativeAdShow()
                    }
                }
            }
        }

    }

    private fun setCleanBtn() {
        adapter?.list?.let { itemList ->
            val isEnabled = itemList.any {
                it.children.any { c -> c.isSelected }
            }
            binding.btnClean.isEnabled = isEnabled
            binding.btnClean.alpha = if (isEnabled) 1f else 0.4f
        }
    }

    private fun setUpAdapter() {
        with(binding) {
            adapter = DuplicateFileCleanGroupAdapter(this@DuplicateFileCleanActivity, allDuplicateFileList,
                changeListener = {
                    setCleanBtn()
                },
                clickListener = {
                    opFile(it.filePath, it.mimeType)
                })

            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@DuplicateFileCleanActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }


    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    private var activeJob: Job? = null
    private val PARTIAL_HASH_THRESHOLD = 20 * 1024 * 1024L // 超过这个大小就只 hash 前 10MB
    private val MAX_SIZE_TO_HASH = 10 * 1024 * 1024

    private fun fetchDuplicateFiles(callback: Callback) = runCatching {
        activeJob?.cancel()
        activeJob = lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            timeTag = System.currentTimeMillis()
            allDuplicateFileList.clear()
            handleDuplicateFiles(executeFileScan())
            callback()
        }
    }

    private suspend fun handleDuplicateFiles(fileList: List<DuplicateFileSub>) = withContext(Dispatchers.IO) {

        if (fileList.isEmpty()) return@withContext
        runCatching {
            val potentialDuplicates = fileList.groupBy { it.fileSize }.filterValues { it.size > 1 }.values.flatten()
            val writableFiles = potentialDuplicates.filter { File(it.filePath).run { exists() && !isDirectory && canWrite() } }
            val hashedFiles = writableFiles.map { async { calculateFileHash(it) } }.awaitAll().groupBy { it.fileHash }.filterValues { it.size > 1 }
            val sortedFiles = hashedFiles.entries.sortedByDescending { it.value.firstOrNull()?.fileSize ?: 0L }.flatMap { it.value }
            updateFileList(sortedFiles)
        }
    }

    private suspend fun calculateFileHash(fileSub: DuplicateFileSub): DuplicateFileSub = withContext(Dispatchers.IO) {

        val fileInstance = File(fileSub.filePath)
        if (!fileInstance.exists() || fileInstance.isDirectory) {
            return@withContext fileSub
        }
        val fileSize = fileInstance.length()
        runCatching {
            val digest = MessageDigest.getInstance("MD5")
            FileInputStream(fileInstance).use { fis ->
                val buffer = ByteArray(1024)
                var totalBytesRead = 0
                var bytesRead: Int

                // 若文件大于阈值，则只 hash 前 MAX_SIZE_TO_HASH 大小
                if (fileSize > PARTIAL_HASH_THRESHOLD) {
                    while (
                        fis.read(buffer).also { bytesRead = it } != -1 &&
                        totalBytesRead < MAX_SIZE_TO_HASH
                    ) {
                        val lengthToUpdate = (MAX_SIZE_TO_HASH - totalBytesRead).coerceAtMost(bytesRead)
                        digest.update(buffer, 0, lengthToUpdate)
                        totalBytesRead += bytesRead
                    }
                } else {
                    // 否则就整文件全部做哈希
                    while (fis.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }

                // 生成 MD5 十六进制字符串 + 文件大小
                val hexHash = digest.digest().joinToString("") { "%02x".format(it) }
                fileSub.fileHash = "$hexHash-$fileSize"
            }
        }.onFailure {
            it.printStackTrace()
        }
        return@withContext fileSub
    }


    private fun updateFileList(fileList: List<DuplicateFileSub>) {
        allDuplicateFileList.addAll(
            fileList.groupBy { it.fileHash }
                .map { (_, group) ->
                    val sortedGroup = group.sortedByDescending { it.addTime }
                    sortedGroup.first().apply {
                        isLatest = true
                        isSelected = false
                    }
                    sortedGroup.last().apply {
                        isOldest = true
                    }
                    DuplicateFileGroup(sortedGroup)
                }
        )
    }


    private fun executeFileScan(): List<DuplicateFileSub> = runCatching {

        val fileUri = MediaStore.Files.getContentUri("external")
        val projectionColumns = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        contentResolver?.query(fileUri, projectionColumns, null, null, "${MediaStore.Files.FileColumns.SIZE} ASC")?.use { cursor ->
            if (!cursor.moveToFirst()) return@use emptyList<DuplicateFileSub>()
            val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val mimeTypeIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

            buildList {
                do {
                    val path = cursor.getStringOrNull(pathIndex).orEmpty()
                    val size = cursor.getLongOrNull(sizeIndex) ?: 0L
                    val date = cursor.getLongOrNull(dateIndex) ?: 0L
                    val mimeType = cursor.getStringOrNull(mimeTypeIndex).orEmpty()
                    if (size > 0 && File(path).exists()) {
                        add(
                            DuplicateFileSub(
                                fileName = File(path).name,
                                filePath = path,
                                fileSize = size,
                                addTime = date,
                                fileSizeStr = Formatter.formatFileSize(this@DuplicateFileCleanActivity, size),
                                mimeType = mimeType.ifEmpty { "*/*" })
                        )
                    }
                } while (cursor.moveToNext())
            }
        } ?: emptyList()

    }.getOrElse { e ->
        e.printStackTrace()
        emptyList()
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
            adState.showFullScreenAd(this@DuplicateFileCleanActivity, "fc_scan_int") { onComplete() }
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
                    adState.showNativeAd(this@DuplicateFileCleanActivity, binding.adContainer, "fc_scan_nat") {
                        ad = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
        ad?.destroy()
    }

}