package com.clean.filecleaner.ui.module

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import com.clean.filecleaner.R
import com.clean.filecleaner.data.PRIVACY_POLICY
import com.clean.filecleaner.data.USER_TERMS
import com.clean.filecleaner.data.WEB_URL
import com.clean.filecleaner.databinding.ActivitySettingBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.BaseActivity

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivitySettingBinding = ActivitySettingBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {

            binding.toolbar.title.text = getString(R.string.setting)
            onBackPressedDispatcher.addCallback {
                startActivity(Intent(this@SettingActivity, MainActivity::class.java))
                finish()
            }

            binding.toolbar.ivLeft.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            userTerms.setOnClickListener {
                startActivity(Intent(this@SettingActivity, WebActivity::class.java).apply {
                    putExtra(WEB_URL, USER_TERMS)
                })
            }

            privacyPolicy.setOnClickListener {
                startActivity(Intent(this@SettingActivity, WebActivity::class.java).apply {
                    putExtra(WEB_URL, PRIVACY_POLICY)
                })
            }

            noticeSet.setOnClickListener {
                startActivity(Intent(this@SettingActivity, NoticeSettingActivity::class.java))
            }

        }


    }
}


