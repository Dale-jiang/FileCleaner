package com.clean.filecleaner.ui.module

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityNoticeSettingBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.notification.BarNotificationCenter
import com.clean.filecleaner.utils.AppPreferences

class NoticeSettingActivity : BaseActivity<ActivityNoticeSettingBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityNoticeSettingBinding = ActivityNoticeSettingBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {

            toolbar.title.text = getString(R.string.notification_settings)
            onBackPressedDispatcher.addCallback {
                startActivity(Intent(this@NoticeSettingActivity, MainActivity::class.java))
                finish()
            }

            toolbar.ivLeft.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }


            trafficSwitch.isChecked = AppPreferences.networkTrafficSwitch
            setTrafficSwitchStatus(trafficSwitch.isChecked)

            trafficSwitch.setOnCheckedChangeListener { _, isChecked ->
                AppPreferences.networkTrafficSwitch = isChecked
                setTrafficSwitchStatus(isChecked)
                BarNotificationCenter.updateNotice()
                if (isChecked) {
                    DataReportingUtils.postCustomEvent("nitifibar_net_on")
                } else {
                    DataReportingUtils.postCustomEvent("nitifibar_net_off")
                }
            }
        }

    }


    private fun setTrafficSwitchStatus(status: Boolean) {
        binding.trafficSwitch.trackTintList = ColorStateList.valueOf(
            ContextCompat.getColor(this, if (status) R.color.colorPrimary else R.color.color_ebeef4)
        )
    }
}


