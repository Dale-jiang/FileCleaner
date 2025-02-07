package com.clean.filecleaner.ui.module.clean.recent

import android.os.Bundle
import com.clean.filecleaner.databinding.ActivityAppRecentInfoBinding
import com.clean.filecleaner.databinding.ActivityAppRecentInfoPreBinding
import com.clean.filecleaner.ext.hasUsagePermissions
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.ui.base.UsagePermissionBaseActivity

class AppRecentInfoActivity : UsagePermissionBaseActivity<ActivityAppRecentInfoBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityAppRecentInfoBinding = ActivityAppRecentInfoBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {

    }
}


