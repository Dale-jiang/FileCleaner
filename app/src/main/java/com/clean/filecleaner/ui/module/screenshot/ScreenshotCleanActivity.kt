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
        onBackPressedDispatcher.addCallback {
            allScreenshotList.clear()
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
                leftBtn = getString(R.string.delete),
                rightBtn = getString(R.string.cancel),
                cancelable = true,
                leftClick = {
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
                binding.bottomView.isVisible = allScreenshotList.isNotEmpty()
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

    override fun onDestroy() {
        super.onDestroy()
        stopLoadingAnim()
    }

}