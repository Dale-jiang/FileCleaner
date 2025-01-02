package com.clean.filecleaner.ui.module.junk

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.addCallback
import com.clean.filecleaner.databinding.ActivityJunkSearchEndBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

class JunkSearchEndActivity : BaseActivity<ActivityJunkSearchEndBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.topLayout, binding.root)
    override fun inflateViewBinding(): ActivityJunkSearchEndBinding = ActivityJunkSearchEndBinding.inflate(layoutInflater)

    private fun initBackListeners() {
        onBackPressedDispatcher.addCallback {
            onBackPressedDispatcher.addCallback {
                finish()
            }
        }
        binding.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun initView(savedInstanceState: Bundle?) {

        with(binding) {

//            recyclerView.itemAnimator = null
//            recyclerView.adapter = adapter
//            val controller = AnimationUtils.loadLayoutAnimation(this@JunkSearchEndActivity, R.anim.recyclerview_animation_controller)
//            recyclerView.layoutAnimation = controller
//            recyclerView.scheduleLayoutAnimation()

            initBackListeners()

        }

    }


}