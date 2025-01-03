package com.clean.filecleaner.ui.module.screenshot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.Formatter
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityScreenshotCleanBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
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

    companion object {
        val allScreenshotList = mutableListOf<ScreenshotCleanParent>()
    }

    private fun setBackListener() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@ScreenshotCleanActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.allChecked.setOnClickListener {
            selectAllBtnClick()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {

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

        val totalSize = allScreenshotList.sumOf { parent ->
            parent.children.sumOf { child -> if (child.isSelected) child.size else 0L }
        }

        val hasSelectedItems = totalSize > 0
        val isListEmpty = allScreenshotList.isEmpty()

        with(binding) {
            this.totalSize.text = Formatter.formatFileSize(this@ScreenshotCleanActivity, totalSize)
            btnClean.apply {
                isEnabled = hasSelectedItems
                alpha = if (hasSelectedItems) 1f else 0.3f
            }
            allChecked.apply {
                isEnabled = !isListEmpty
                if (!hasSelectedItems) isChecked = false
            }
            emptyView.isVisible = isListEmpty
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun selectAllBtnClick() {
        var totalSize = 0L
        allScreenshotList.forEach {
            val status = binding.allChecked.isChecked
            if (status) it.select() else it.deselect()
            it.children.forEach { c ->
                if (status) c.select() else c.deselect()
                if (c.isSelected) totalSize += c.size
            }
        }
        binding.totalSize.text = Formatter.formatFileSize(this@ScreenshotCleanActivity, totalSize)
        binding.btnClean.apply {
            isEnabled = totalSize > 0
            alpha = if (totalSize > 0) 1f else 0.3f
        }
        adapter?.notifyDataSetChanged()
    }

    private suspend fun fetchScreenshotList() = withContext(Dispatchers.IO + SupervisorJob()) {
        try {
            timeTag = System.currentTimeMillis()
            allScreenshotList.clear()
            val directoryName = if (AndroidVersionUtils.isAndroid10OrAbove()) {
                Environment.DIRECTORY_SCREENSHOTS
            } else {
                "Screenshots"
            }
            val projection = arrayOf(
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE
            )
            val imgList = mutableListOf<ScreenshotCleanSub>()
            val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(directoryName)

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
                    val datetime = cursor.getLong(dateModifiedCol) * 1000L
                    val path = cursor.getString(dataCol)
                    val displayName = cursor.getString(displayNameCol)
                    val size = cursor.getLong(sizeCol)

                    val file = File(path)
                    if (file.exists() && file.isFile && (path.endsWith(".png", ignoreCase = true) || path.endsWith(".jpg", ignoreCase = true))) {
                        imgList.add(ScreenshotCleanSub(displayName, size, datetime, path))
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

                stopLoadingAnim()
                TransitionManager.beginDelayedTransition(binding.root)
                binding.loadingView.isVisible = false
                binding.emptyView.isVisible = allScreenshotList.isEmpty()
                setUpAdapter()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stopLoadingAnim() {
        super.stopLoadingAnim()
        binding.ivLoading.stopRotatingWithRotateAnimation()
    }

}