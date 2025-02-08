package com.clean.filecleaner.ui.module.clean.recent.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.FragmentLaunchesBinding
import com.clean.filecleaner.ui.base.BaseFragment
import com.clean.filecleaner.ui.module.SettingTipsActivity
import com.clean.filecleaner.ui.module.clean.recent.AppLaunchInfo
import com.clean.filecleaner.ui.module.clean.recent.LaunchType
import com.clean.filecleaner.ui.module.clean.recent.RecentAppHelper.getDateRangePairByIndex
import com.clean.filecleaner.ui.module.clean.recent.adapter.LaunchesAdapter
import com.clean.filecleaner.ui.module.clean.recent.viewmodel.LaunchesInfoViewModel
import com.clean.filecleaner.ui.module.dialog.AppLaunchesFilterDialog
import com.clean.filecleaner.utils.AppLifeHelper.jumpToSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LaunchesFragment : BaseFragment<FragmentLaunchesBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLaunchesBinding {
        return FragmentLaunchesBinding.inflate(layoutInflater)
    }

    private var currentDateIndex = 1
    private var launchesType: LaunchType = LaunchType.TOTAL
    private val viewModel by viewModels<LaunchesInfoViewModel>()

    private val openSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        jumpToSettings = false
        fetchData()
    }

    private val launchAdapter by lazy {
        LaunchesAdapter(requireActivity()) {
            openAppSettingPage(it.packageName)
        }
    }

    private fun setListeners() {
        binding.apply {
            btnTotal.setOnClickListener { updateTypeBtn(LaunchType.TOTAL) }
            btnForeground.setOnClickListener { updateTypeBtn(LaunchType.FOREGROUND) }
            btnBackground.setOnClickListener { updateTypeBtn(LaunchType.BACKGROUND) }

            btnDate.setOnClickListener {
                AppLaunchesFilterDialog(requireActivity()) { index, str ->
                    date.text = str
                    currentDateIndex = index
                    fetchData()
                }.show()
            }

            viewModel.launchInfosLiveData.observe(viewLifecycleOwner) { list ->
                binding.progressbar.isVisible = false
                with(binding) {
                    totalCount.text = "${list.sumOf { it.totalCount }}"
                    foregroundCount.text = "${list.sumOf { it.foreground }}"
                    backgroundCount.text = "${list.sumOf { it.background }}"
                }

                updateRecyclerView(list)
            }
        }

    }

    private fun updateTypeBtn(type: LaunchType) {
        launchesType = type
        binding.apply {
            btnTotal.isSelected = launchesType == LaunchType.TOTAL
            btnForeground.isSelected = launchesType == LaunchType.FOREGROUND
            btnBackground.isSelected = launchesType == LaunchType.BACKGROUND
            viewModel.launchInfosLiveData.value?.apply {
                updateRecyclerView(this)
            }
        }

    }

    override fun initView(savedInstanceState: Bundle?) {

        binding.recyclerView.apply {
            itemAnimator = null
            adapter = launchAdapter
        }
        updateTypeBtn(launchesType)
        setListeners()
        fetchData()
    }

    private fun fetchData() {
        val (start, end) = getDateRangePairByIndex(currentDateIndex)
        binding.progressbar.isVisible = true
        viewModel.fetchAppLaunchInfos(start, end)
    }

    private fun updateRecyclerView(listData: MutableList<AppLaunchInfo>) {
        runCatching {

            lifecycleScope.launch(Dispatchers.Main) {

                val finalList = when (launchesType) {
                    LaunchType.BACKGROUND -> listData.filter { it.background != 0 }.sortedByDescending { it.background }.toMutableList()
                    LaunchType.FOREGROUND -> listData.filter { it.foreground != 0 }.sortedByDescending { it.foreground }.toMutableList()
                    LaunchType.TOTAL -> listData.filter { it.totalCount != 0 }.sortedByDescending { it.totalCount }.toMutableList()
                }

                launchAdapter.initList(launchesType, mutableListOf())
                delay(150L)
                binding.recyclerView.apply {
                    TransitionManager.beginDelayedTransition(this)
                }
                binding.emptyView.isVisible = finalList.isEmpty()
                launchAdapter.initList(launchesType, finalList)
            }
        }
    }

    private fun openAppSettingPage(packageName: String) = runCatching {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        jumpToSettings = true
        openSettingsLauncher.launch(intent)
        lifecycleScope.launch(Dispatchers.Main) {
            delay(300)
            startActivity(Intent(requireActivity(), SettingTipsActivity::class.java).apply {
                putExtra("SETTING_MESSAGE", getString(R.string.tap_force_stop_to_completely_close_the_running_app))
            })

        }
    }.onFailure {
        LogUtils.e("openAppSetting-Error", "Error opening app settings", it)
    }


}