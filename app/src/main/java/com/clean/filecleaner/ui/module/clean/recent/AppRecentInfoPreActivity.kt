package com.clean.filecleaner.ui.module.clean.recent

import android.content.Intent
import android.os.Bundle
import com.clean.filecleaner.databinding.ActivityAppRecentInfoPreBinding
import com.clean.filecleaner.ext.hasUsagePermissions
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.UsagePermissionBaseActivity

class AppRecentInfoPreActivity : UsagePermissionBaseActivity<ActivityAppRecentInfoPreBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityAppRecentInfoPreBinding = ActivityAppRecentInfoPreBinding.inflate(layoutInflater)

    private var hasRequest = false

    override fun onResume() {
        super.onResume()

        if (!hasRequest) {
            hasRequest = true
            requestPermissions {
                startActivity(Intent(this, AppRecentInfoActivity::class.java))
                finish()
            }
        } else {
            if (hasUsagePermissions()) {
                startActivity(Intent(this, AppRecentInfoActivity::class.java))
                finish()
            } else finish()
        }

    }


    override fun initView(savedInstanceState: Bundle?) {

    }
}


