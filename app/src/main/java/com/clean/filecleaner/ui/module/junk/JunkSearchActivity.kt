package com.clean.filecleaner.ui.module.junk

import android.os.Bundle
import android.view.animation.AnimationUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityJunkSearchBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.junk.adapter.JunkSearchAdapter
import com.clean.filecleaner.ui.module.junk.bean.JunkSearchItem

class JunkSearchActivity : BaseActivity<ActivityJunkSearchBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityJunkSearchBinding = ActivityJunkSearchBinding.inflate(layoutInflater)

    private val adapter by lazy {
        val list = emptyList<JunkSearchItem>()
        JunkSearchAdapter(this, list)
    }

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {

            binding.recyclerView.itemAnimator = null
            binding.recyclerView.adapter = adapter

            val controller = AnimationUtils.loadLayoutAnimation(this@JunkSearchActivity, R.anim.recyclerview_animation_controller)
            binding.recyclerView.layoutAnimation = controller
            binding.recyclerView.scheduleLayoutAnimation()
        }


    }
}