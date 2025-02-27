package com.clean.filecleaner.ui.module.antivirus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ToastUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.ActivityVirusListBinding
import com.clean.filecleaner.ext.immersiveMode
import com.clean.filecleaner.report.reporter.DataReportingUtils
import com.clean.filecleaner.ui.ad.IAd
import com.clean.filecleaner.ui.ad.adManagerState
import com.clean.filecleaner.ui.ad.canShow
import com.clean.filecleaner.ui.ad.hasReachedUnusualAdLimit
import com.clean.filecleaner.ui.ad.showNativeAd
import com.clean.filecleaner.ui.ad.waitAdLoading
import com.clean.filecleaner.ui.base.BaseActivity
import com.clean.filecleaner.ui.module.MainActivity
import com.clean.filecleaner.ui.module.dialog.CommonDialog
import com.clean.filecleaner.utils.AppLifeHelper.jumpToSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VirusListActivity : BaseActivity<ActivityVirusListBinding>() {
    override fun setupImmersiveMode() = immersiveMode(binding.root)
    override fun inflateViewBinding(): ActivityVirusListBinding = ActivityVirusListBinding.inflate(layoutInflater)

    private var adapter: VirusListAdapter? = null
    private var packageNameTemp: String = ""

    private val uninstallLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        jumpToSettings = false
        if (AppUtils.isAppInstalled(packageNameTemp).not()) {
            updateList(packageNameTemp)
        }
    }

    private fun setListener() {
        onBackPressedDispatcher.addCallback {
            startActivity(Intent(this@VirusListActivity, MainActivity::class.java))
            finish()
        }

        binding.toolbar.ivLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun initView(savedInstanceState: Bundle?) {

        setListener()
        nativeAdShow()

        with(binding) {
            toolbar.title.text = getString(R.string.antivirus)
            threatsNum.text = getString(R.string.threats_found, allVirusList.size)
        }

        if (allVirusList.isNotEmpty()) {
            setUpAdapter(allVirusList)
        }

    }

    private fun setUpAdapter(finalList: MutableList<VirusInfo>) {

        with(binding) {
            adapter = VirusListAdapter(this@VirusListActivity, list = finalList) {
                if (it.isApp) {
                    packageNameTemp = it.packageName
                    unInstallApp(packageNameTemp)
                } else {
                    CommonDialog(
                        title = getString(R.string.confirm),
                        message = getString(R.string.sure_to_delete),
                        leftBtn = getString(R.string.cancel),
                        rightBtn = getString(R.string.delete),
                        rightClick = { deleteVirus(it) }).show(supportFragmentManager, "CommonDialog")
                }
            }

//            recyclerView.itemAnimator = null
            recyclerView.adapter = adapter
            val controller = AnimationUtils.loadLayoutAnimation(this@VirusListActivity, R.anim.recyclerview_animation_controller)
            recyclerView.layoutAnimation = controller
            recyclerView.scheduleLayoutAnimation()
        }
    }

    private fun deleteVirus(info: VirusInfo) {
        lifecycleScope.launch(Dispatchers.IO + SupervisorJob()) {
            runCatching {
                val result = File(info.path).deleteRecursively()
                if (result) {
                    withContext(Dispatchers.Main) {
                        updateList(info.path)
                    }
                } else ToastUtils.showLong(getString(R.string.delete_failed))
            }
        }
    }

    private fun updateList(path: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            runCatching {
                val list = adapter?.list.orEmpty().toMutableList()
                val index = list.indexOfFirst { it.path == path || it.packageName == path }

                if (index != -1) {
                    adapter?.list?.removeAt(index)
                    adapter?.notifyItemRemoved(index)
                }

                val remainingItems = adapter?.list?.size ?: 0
                binding.threatsNum.text = getString(R.string.threats_found, remainingItems)

                if (remainingItems <= 0) {

                    startActivity(Intent(this@VirusListActivity, AntivirusEndActivity::class.java).apply {
                        putExtra("MESSAGE", getString(R.string.all_threats_resolved))
                    })

                    finish()
                }
            }
        }
    }


    private fun unInstallApp(packageName: String) {
        kotlin.runCatching {
            jumpToSettings = true
            uninstallLauncher.launch(Intent(Intent.ACTION_DELETE, Uri.parse("package:${packageName}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
            })
        }
    }


    private var ad: IAd? = null
    private fun nativeAdShow() {
        if (adManagerState.hasReachedUnusualAdLimit()) return
        DataReportingUtils.postCustomEvent("fc_ad_chance", hashMapOf("ad_pos_id" to "fc_scan_nat"))
        val adState = adManagerState.fcScanNatState
        adState.waitAdLoading(this) {
            lifecycleScope.launch {
                while (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) delay(210L)
                if (adState.canShow()) {
                    ad?.destroy()
                    adState.showNativeAd(this@VirusListActivity, binding.adContainer, "fc_scan_nat") {
                        ad = it
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ad?.destroy()
        stopLoadingAnim()
    }

}