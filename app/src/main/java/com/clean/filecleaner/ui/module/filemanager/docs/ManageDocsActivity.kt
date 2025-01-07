package com.clean.filecleaner.ui.module.filemanager.docs

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.Formatter
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.clean.filecleaner.R
import com.clean.filecleaner.data.docMatchList
import com.clean.filecleaner.databinding.ActivityManageDocsBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.opFile
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.ui.module.filemanager.FileCleanEndActivity
import com.clean.filecleaner.ui.module.filemanager.FileInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ManageDocsActivity : BaseActivity<ActivityManageDocsBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityManageDocsBinding = ActivityManageDocsBinding.inflate(layoutInflater)

    private var adapter: ManageDocsAdapter? = null
    private var timeTag = 0L

    companion object {
        val allDocsList = mutableListOf<FileInfo>()
    }


    private fun setListeners() {
        onBackPressedDispatcher.addCallback {
            kotlin.runCatching {
                allDocsList.clear()
            }
            startActivity(Intent(this@ManageDocsActivity, MainActivity::class.java))
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
                        putExtra("FILE_TYPES", FileCleanEndActivity.docs)
                    })
                    finish()
                }
            ).show(supportFragmentManager, "CommonDialog")
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

        setListeners()

        with(binding) {
            toolbar.title.text = getString(R.string.docs)
            ivLoading.startRotatingWithRotateAnimation()
            showLoadingAnimation(preStr = getString(R.string.scanning)) {
                tvLoading.text = it
            }
        }

        lifecycleScope.launch {
            fetchDocsList()
        }
    }

    private fun setUpAdapter() {
        with(binding) {
            adapter = ManageDocsAdapter(this@ManageDocsActivity, list = allDocsList,
                clickListener = {
                    opFile(it.path, it.mimetype)
                }, checkboxListener = {
                    setCleanBtn()
                })
            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@ManageDocsActivity, R.anim.recyclerview_animation_controller)
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

    private suspend fun fetchDocsList() = withContext(Dispatchers.IO + SupervisorJob()) {
        try {
            timeTag = System.currentTimeMillis()
            allDocsList.clear()

            val projection = arrayOf(
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED,
            )

            val placeholders = docMatchList.joinToString(",") { "?" }
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN ($placeholders)"

            contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                docMatchList,
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
                    val dateModified = cursor.getLong(dateModifiedCol) * 1000L
                    val dateAdded = cursor.getLong(dateAddedCol) * 1000L
                    val path = cursor.getString(dataCol)
                    val displayName = cursor.getString(displayNameCol)
                    val size = cursor.getLong(sizeCol)
                    val mimeType = cursor.getString(mimeTypeCol)

                    val file = File(path)
                    if (file.exists() && file.isFile) {
                        docList.add(
                            FileInfo(
                                name = displayName,
                                size = size,
                                sizeString = Formatter.formatFileSize(this@ManageDocsActivity, size),
                                updateTime = dateModified,
                                addTime = dateAdded,
                                path = path,
                                mimetype = mimeType
                            )
                        )
                    }
                }

                allDocsList.addAll(docList)
            }

            val delayTime = timeTag + 2000 - System.currentTimeMillis()
            if (delayTime > 0) delay(delayTime)

            withContext(Dispatchers.Main) {

                stopLoadingAnim()
//                TransitionManager.beginDelayedTransition(binding.root)
                binding.num.text = "${allDocsList.size}"
                binding.loadingView.isVisible = false
                binding.bottomView.isVisible = allDocsList.isNotEmpty()
                binding.emptyView.isVisible = allDocsList.isEmpty()
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