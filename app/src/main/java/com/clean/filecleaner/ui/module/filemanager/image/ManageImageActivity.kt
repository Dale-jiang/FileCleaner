package com.clean.filecleaner.ui.module.filemanager.image

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
import com.clean.filecleaner.databinding.ActivityManageImageBinding
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
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.ui.module.filemanager.FileCleanEndActivity
import com.clean.filecleaner.ui.module.filemanager.FileInfo
import com.clean.filecleaner.ui.module.filemanager.ManageMediaGroupAdapter
import com.clean.filecleaner.ui.module.filemanager.MediaInfoParent
import com.clean.filecleaner.ui.module.filemanager.allMediaList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ManageImageActivity : BaseActivity<ActivityManageImageBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityManageImageBinding = ActivityManageImageBinding.inflate(layoutInflater)

    private var adapter: ManageMediaGroupAdapter? = null
    private var timeTag = 0L


    private fun setListeners() {
        onBackPressedDispatcher.addCallback {
            kotlin.runCatching {
                allMediaList.clear()
            }
            startActivity(Intent(this@ManageImageActivity, MainActivity::class.java))
            finish()
        }

        binding.loadingView.setOnClickListener {  }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnClean.setOnClickListener {
            CommonDialog(title = getString(R.string.warning),
                message = getString(R.string.do_you_wish_to_delete_this),
                rightBtn = getString(R.string.delete),
                leftBtn = getString(R.string.cancel),
                cancelable = true,
                rightClick = {
                    startActivity(Intent(this, FileCleanEndActivity::class.java).apply {
                        putExtra("FILE_TYPES", FileCleanEndActivity.image)
                    })
                    finish()
                }).show(supportFragmentManager, "CommonDialog")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        adManagerState.fcResultIntState.loadAd(this)
        adManagerState.fcResultNatState.loadAd(this)

        setListeners()

        with(binding) {
            toolbar.title.text = getString(R.string.image)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

        lifecycleScope.launch {
            fetchAudioList()
        }
    }

    private fun setUpAdapter() {
        with(binding) {
            adapter = ManageMediaGroupAdapter(this@ManageImageActivity, list = allMediaList, false, clickListener = {
                setCleanBtn()
            })
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@ManageImageActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }

    private fun setCleanBtn() {
        adapter?.list?.let { itemList ->
            val isEnabled = itemList.any { c -> c.children.any { it.isSelected } }
            binding.btnClean.isEnabled = isEnabled
            binding.btnClean.alpha = if (isEnabled) 1f else 0.4f
        }
    }

    private suspend fun fetchAudioList() = withContext(Dispatchers.IO + SupervisorJob()) {
        try {
            timeTag = System.currentTimeMillis()
            allMediaList.clear()
            var imageNum = 0

            val projection = arrayOf(
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE
            )

            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val imageList = mutableListOf<FileInfo>()

                val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val displayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val dateModified = cursor.getLongOrNull(dateModifiedCol) ?: 0L
                    val dateAdded = cursor.getLongOrNull(dateAddedCol) ?: 0L
                    val path = cursor.getStringOrNull(dataCol).orEmpty()
                    val displayName = cursor.getStringOrNull(displayNameCol).orEmpty()
                    val size = cursor.getLongOrNull(sizeCol) ?: 0L
                    val mimeType = cursor.getStringOrNull(mimeTypeCol).orEmpty()

                    val file = File(path)
                    if (file.exists() && file.isFile) {
                        imageList.add(
                            FileInfo(
                                name = displayName,
                                size = size,
                                sizeString = Formatter.formatFileSize(this@ManageImageActivity, size),
                                updateTime = dateModified * 1000L,
                                addTime = dateAdded * 1000L,
                                path = path,
                                mimetype = mimeType.ifEmpty { "*/*" }
                            )
                        )
                    }
                }
                imageNum = imageList.size
                imageList.groupBy { it.timeStr }.forEach { (_, list) ->
                    allMediaList.add(MediaInfoParent(children = list.toMutableList(), time = list.first().addTime))
                }

            }

            val delayTime = timeTag + 2000 - System.currentTimeMillis()
            if (delayTime > 0) delay(delayTime)

            withContext(Dispatchers.Main) {

                fullScreenAdShow {
                    stopLoadingAnim()
                    if (allMediaList.isEmpty()) {
                        TransitionManager.beginDelayedTransition(binding.root)
                    }
                    binding.num.text = "$imageNum"
                    binding.loadingView.isVisible = false
                    binding.bottomView.isVisible = allMediaList.isNotEmpty()
                    binding.emptyView.isVisible = allMediaList.isEmpty()
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
                delay(210L)
            }
            adState.showFullScreenAd(this@ManageImageActivity, "fc_scan_int") { onComplete() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }
}