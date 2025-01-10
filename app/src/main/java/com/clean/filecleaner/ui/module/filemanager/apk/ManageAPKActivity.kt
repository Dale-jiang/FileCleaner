package com.clean.filecleaner.ui.module.filemanager.apk

import android.content.Intent
import android.os.Bundle
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
import com.clean.filecleaner.data.apkMatchList
import com.clean.filecleaner.databinding.ActivityManageApksBinding
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
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.ui.module.filemanager.FileCleanEndActivity
import com.clean.filecleaner.ui.module.filemanager.FileInfo
import com.clean.filecleaner.ui.module.filemanager.allFilesContainerList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ManageAPKActivity : BaseActivity<ActivityManageApksBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityManageApksBinding = ActivityManageApksBinding.inflate(layoutInflater)

    private var adapter: ManageAPKAdapter? = null
    private var timeTag = 0L

    private fun setListeners() {
        onBackPressedDispatcher.addCallback {
            kotlin.runCatching {
                allFilesContainerList.clear()
            }
            startActivity(Intent(this@ManageAPKActivity, MainActivity::class.java))
            finish()
        }

        binding.loadingView.setOnClickListener { }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnClean.setOnClickListener {
            CommonDialog(
                title = getString(R.string.warning),
                message = getString(R.string.do_you_wish_to_delete_this),
                leftBtn = getString(R.string.delete),
                rightBtn = getString(R.string.cancel),
                cancelable = true,
                leftClick = {
                    startActivity(Intent(this, FileCleanEndActivity::class.java).apply {
                        putExtra("FILE_TYPES", FileCleanEndActivity.apk)
                    })
                    finish()
                }
            ).show(supportFragmentManager, "CommonDialog")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this)

        setListeners()

        with(binding) {
            toolbar.title.text = getString(R.string.apk)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

        lifecycleScope.launch {
            fetchAPKList()
        }
    }

    private fun setUpAdapter() {
        with(binding) {
            adapter = ManageAPKAdapter(this@ManageAPKActivity, list = allFilesContainerList,
                clickListener = {
                    opFile(it.path, it.mimetype)
                }, checkboxListener = {
                    setCleanBtn()
                })
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@ManageAPKActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }

    private fun setCleanBtn() {
        adapter?.list?.let { itemList ->
            val isEnabled = itemList.any { c -> c.isSelected }
            binding.btnClean.isEnabled = isEnabled
            binding.btnClean.alpha = if (isEnabled) 1f else 0.4f
        }
    }

    private suspend fun fetchAPKList() = withContext(Dispatchers.IO + SupervisorJob()) {
        try {
            timeTag = System.currentTimeMillis()
            allFilesContainerList.clear()

            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
            )

            val placeholders = apkMatchList.joinToString(",") { "?" }
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN ($placeholders)"

            contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                apkMatchList,
                "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
            )?.use { cursor ->
                val docList = mutableListOf<FileInfo>()

                val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val displayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val dateModified = cursor.getLongOrNull(dateModifiedCol) ?: 0L
                    val dateAdded = cursor.getLongOrNull(dateAddedCol) ?: 0L
                    val path = cursor.getStringOrNull(dataCol).orEmpty()
                    val displayName = cursor.getStringOrNull(displayNameCol).orEmpty()
                    val size = cursor.getLongOrNull(sizeCol) ?: 0L
                    val mimeType = cursor.getStringOrNull(mimeTypeCol).orEmpty()

                    val file = File(path)
                    if (file.exists() && file.isFile) {
                        docList.add(
                            FileInfo(
                                name = displayName,
                                size = size,
                                sizeString = Formatter.formatFileSize(this@ManageAPKActivity, size),
                                updateTime = dateModified * 1000L,
                                addTime = dateAdded * 1000L,
                                path = path,
                                mimetype = mimeType.ifEmpty { "*/*" }
                            )
                        )
                    }
                }

                allFilesContainerList.addAll(docList)
            }

            val delayTime = timeTag + 2000 - System.currentTimeMillis()
            if (delayTime > 0) delay(delayTime)

            withContext(Dispatchers.Main) {
                fullScreenAdShow {
                    stopLoadingAnim()
                    if (allFilesContainerList.isEmpty()) {
                        TransitionManager.beginDelayedTransition(binding.root)
                    }
                    binding.num.text = "${allFilesContainerList.size}"
                    binding.loadingView.isVisible = false
                    binding.bottomView.isVisible = allFilesContainerList.isNotEmpty()
                    binding.emptyView.isVisible = allFilesContainerList.isEmpty()
                    setUpAdapter()
                    setCleanBtn()
                }
            }

        } catch (e: Throwable) {
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
                delay(200L)
            }
            adState.showFullScreenAd(this@ManageAPKActivity, "fc_scan_int") { onComplete() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }
}