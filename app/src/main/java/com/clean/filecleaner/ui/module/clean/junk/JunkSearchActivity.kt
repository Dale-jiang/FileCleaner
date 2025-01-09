package com.clean.filecleaner.ui.module.clean.junk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter.formatFileSize
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityJunkSearchBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.clean.junk.adapter.JunkSearchAdapter
import com.clean.filecleaner.ui.module.clean.junk.bean.JunkSearchItem
import com.clean.filecleaner.ui.module.clean.junk.bean.JunkType
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.JunkSearchViewModel
import com.clean.filecleaner.ui.module.clean.junk.viewmodel.allJunkDataList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JunkSearchActivity : BaseActivity<ActivityJunkSearchBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.topLayout, binding.root)
    override fun inflateViewBinding(): ActivityJunkSearchBinding = ActivityJunkSearchBinding.inflate(layoutInflater)

    private val viewModel by viewModels<JunkSearchViewModel>()

    private val junkTypeToIndexMap = mapOf(
        JunkType.APP_CACHE to 0,
        JunkType.APK_FILES to 1,
        JunkType.LOG_FILES to 2,
        JunkType.AD_JUNK to 3,
        JunkType.TEMP_FILES to 4
    )

    private val adapter by lazy {
        val list = listOf(
            JunkSearchItem(getString(R.string.app_cache), R.drawable.svg_app_cache, 0L, true),
            JunkSearchItem(getString(R.string.apk_files), R.drawable.svg_apk_files, 0L, true),
            JunkSearchItem(getString(R.string.log_files), R.drawable.svg_log_files, 0L, true),
            JunkSearchItem(getString(R.string.ad_junk), R.drawable.svg_ad_junk, 0L, true),
            JunkSearchItem(getString(R.string.temp_files), R.drawable.svg_temp_files, 0L, true)
        )
        JunkSearchAdapter(this, list)
    }

    private fun initBackListeners() {
        onBackPressedDispatcher.addCallback {
            onBackPressedDispatcher.addCallback {
                ToastUtils.showLong(getString(R.string.just_a_moment_while_we_scan))
            }
        }
        binding.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {

        with(binding) {

            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@JunkSearchActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()

            initBackListeners()
            viewModel.searchJunk()
        }


        with(viewModel) {
            pathChangeObserver.observe(this@JunkSearchActivity) {
                binding.filePath.text = it
            }

            itemChangeObserver.observe(this@JunkSearchActivity) { item ->
                val index = junkTypeToIndexMap[item.trashType]
                index?.let {
                    val data = adapter.list[it]
                    data.size += item.fileSize
                    adapter.notifyItemChanged(it)
                }
                val totalSize = viewModel.junkDetailsTempList.sumOf { it.fileSize }
                val formattedSize = formatFileSize(this@JunkSearchActivity, totalSize)
                binding.junkSize.text = formattedSize
            }


            createJunkDataListObserver.observe(this@JunkSearchActivity) {

                lifecycleScope.launch {

                    it?.apply {

                        val index = junkTypeToIndexMap[this]

                        index?.let { i ->
                            adapter.list[i].isLoading = false
                            adapter.notifyItemChanged(i)
                        }

                        if (it == JunkType.TEMP_FILES) {
                            delay(100L)
                            if (allJunkDataList.isEmpty()) {
                                startActivity(Intent(this@JunkSearchActivity, JunkCleanEndActivity::class.java))
                                finish()
                            } else {
                                startActivity(Intent(this@JunkSearchActivity, JunkSearchEndActivity::class.java))
                                finish()
                            }
                        }
                    } ?: run {
                        adapter.list.forEach { data -> data.isLoading = false }
                        adapter.notifyDataSetChanged()
                        delay(500L)
                        startActivity(Intent(this@JunkSearchActivity, JunkCleanEndActivity::class.java))
                        finish()
                    }

                }
            }
        }

    }


}