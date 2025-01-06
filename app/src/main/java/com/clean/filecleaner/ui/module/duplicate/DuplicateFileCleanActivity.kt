package com.clean.filecleaner.ui.module.duplicate

import android.os.Bundle
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityDuplicateFileCleanBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

class DuplicateFileCleanActivity : BaseActivity<ActivityDuplicateFileCleanBinding>() {

    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityDuplicateFileCleanBinding = ActivityDuplicateFileCleanBinding.inflate(layoutInflater)
    private var timeTag = 0L

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.title.text = getString(R.string.duplicate_files)
            toolbar.ivRight.setImageResource(R.drawable.svg_duplicate_filter)
        }
    }


}