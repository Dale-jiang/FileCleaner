package com.clean.filecleaner.ui.module.filemanager.audio

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.clean.filecleaner.R
import com.clean.filecleaner.data.audioMatchList
import com.clean.filecleaner.databinding.ActivityManageAudioBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.opFile
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
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

class ManageAudioActivity : BaseActivity<ActivityManageAudioBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityManageAudioBinding = ActivityManageAudioBinding.inflate(layoutInflater)

    private var adapter: ManageAudioAdapter? = null
    private var timeTag = 0L


    private fun setListeners() {
        onBackPressedDispatcher.addCallback {
            kotlin.runCatching {
                allFilesContainerList.clear()
            }
            startActivity(Intent(this@ManageAudioActivity, MainActivity::class.java))
            finish()
        }

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
                        putExtra("FILE_TYPES", FileCleanEndActivity.audio)
                    })
                    finish()
                }
            ).show(supportFragmentManager, "CommonDialog")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        setListeners()

        with(binding) {
            toolbar.title.text = getString(R.string.audio)
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
            adapter = ManageAudioAdapter(this@ManageAudioActivity, list = allFilesContainerList,
                clickListener = {
                    opFile(it.path, it.mimetype)
                }, checkboxListener = {
                    setCleanBtn()
                })
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@ManageAudioActivity, R.anim.recyclerview_animation_controller)
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

    private suspend fun fetchAudioList() = withContext(Dispatchers.IO + SupervisorJob()) {
        try {
            timeTag = System.currentTimeMillis()
            allFilesContainerList.clear()

            val projection = arrayOf(
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE
            )

            val placeholders = audioMatchList.joinToString(",") { "?" }
            val selection = "${MediaStore.Audio.Media.MIME_TYPE} IN ($placeholders)"

            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                audioMatchList,
                "${MediaStore.Audio.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val docList = mutableListOf<FileInfo>()

                val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val displayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

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
                                sizeString = Formatter.formatFileSize(this@ManageAudioActivity, size),
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

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }
}