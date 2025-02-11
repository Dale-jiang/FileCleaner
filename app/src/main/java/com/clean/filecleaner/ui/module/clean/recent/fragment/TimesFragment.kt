package com.clean.filecleaner.ui.module.clean.recent.fragment

import android.content.Intent
import android.graphics.DashPathEffect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.LongSparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.util.isNotEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.blankj.utilcode.util.LogUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.FragmentScreenTimeBinding
import com.clean.filecleaner.ext.formatTime
import com.clean.filecleaner.ext.formatToDuration
import com.clean.filecleaner.ui.base.BaseFragment
import com.clean.filecleaner.ui.module.SettingTipsActivity
import com.clean.filecleaner.ui.module.clean.recent.RecentAppHelper.getDateNameByIndex
import com.clean.filecleaner.ui.module.clean.recent.adapter.ScreenTimeAdapter
import com.clean.filecleaner.ui.module.clean.recent.viewmodel.ScreenTimeViewModel
import com.clean.filecleaner.ui.module.dialog.AppLaunchesFilterDialog
import com.clean.filecleaner.utils.AppLifeHelper.jumpToSettings
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimesFragment : BaseFragment<FragmentScreenTimeBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentScreenTimeBinding {
        return FragmentScreenTimeBinding.inflate(layoutInflater)
    }

    private var currentDateIndex = 1
    private val viewModel by viewModels<ScreenTimeViewModel>()

    private val openSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        jumpToSettings = false
    }

    private val screenTimeAdapter by lazy {
        ScreenTimeAdapter(requireActivity()) {
            openAppSettingPage(it.packageName)
        }
    }

    private fun setListeners() {
        binding.apply {
            btnDate.setOnClickListener {
                AppLaunchesFilterDialog(requireActivity()) { index, str ->
                    date.text = str
                    currentDateIndex = index
                    binding.progressbar.isVisible = true
                    viewModel.getRangeTotalByIndex(index)
                    viewModel.fetchListData(index)
                }.show()
            }
        }

        viewModel.apply {
            timeChartData.observe(this@TimesFragment) {
                binding.progressbar.isVisible = false
                initChartData(it)
            }
            appUsageList.observe(this@TimesFragment) { list ->
                lifecycleScope.launch(Dispatchers.Main) {
                    screenTimeAdapter.initList(mutableListOf())
                    delay(150L)
                    binding.recyclerView.apply {
                        TransitionManager.beginDelayedTransition(this)
                    }
                    val newList = list.sortedByDescending { it.duration }.toMutableList()
                    binding.emptyView.isVisible = newList.isEmpty()
                    screenTimeAdapter.initList(newList)
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        with(binding) {

            date.text = getDateNameByIndex(currentDateIndex)
            barChart.setNoDataText("")
            progressbar.isVisible = true
            viewModel.getRangeTotalByIndex(currentDateIndex)
            viewModel.fetchListData(currentDateIndex)

            recyclerView.itemAnimator = null
            recyclerView.adapter = screenTimeAdapter

            setListeners()
        }
    }


//    private fun initChartData(array: LongSparseArray<Long>) {
//        if (array.isNotEmpty()) {
//            val entries = mutableListOf<BarEntry>()
//            val timeList = mutableListOf<Long>()
//            for (index in 0 until array.size()) {
//                timeList.add(array.keyAt(index))
//                entries.add(BarEntry(index.toFloat(), array.valueAt(index).toFloat()))
//            }
//            val dataset = BarDataSet(entries, "AppScreenTimeChart").apply {
//                setDrawValues(false)
//                color = requireActivity().getColor(R.color.colorPrimary)
//            }
//            binding.barChart.run {
//                setScaleEnabled(false)
//                setTouchEnabled(false)
//                isDoubleTapToZoomEnabled = false
//                setDrawGridBackground(false)
//                setNoDataText("")
//                setPadding(0, 0, 0, 0)
//                setFitBars(true)
//                description.isEnabled = false
//                legend.isEnabled = false
//                xAxis.run {
//                    setDrawGridLines(false)
//                    position = XAxis.XAxisPosition.BOTTOM
//                    mLabelHeight = 2
//                    textColor = requireActivity().getColor(R.color.color_999)
//                    axisLineColor = requireActivity().getColor(R.color.color_bcbcbc)
//                    granularity = 1.0f
//                    isGranularityEnabled = true
//                    valueFormatter = object : ValueFormatter() {
//                        override fun getFormattedValue(value: Float): String {
//                            return if (value.toInt() < timeList.size) {
//                                when (currentDateIndex) {
//                                    0, 1, 2 -> (timeList.getOrNull(value.toInt()) ?: 0L).formatTime("HH:mm")
//                                    3 -> (timeList.getOrNull(value.toInt()) ?: 0L).formatTime("MM.dd").removePrefix("0")
//                                    else -> ""
//                                }
//                            } else ""
//                        }
//                    }
//                }
//                axisLeft.axisMinimum = 0f
//                axisLeft.isEnabled = false
//                axisRight.run {
//                    setDrawZeroLine(false)
//                    setDrawLabels(false)
//                    axisLineColor = requireActivity().getColor(R.color.trans)
//                    zeroLineColor = requireActivity().getColor(R.color.trans)
//                    textColor = requireActivity().getColor(R.color.color_999)
//                    gridColor = requireActivity().getColor(R.color.color_bcbcbc)
//                    gridLineWidth = 0f
//                    axisMinimum = 0f
//                    setLabelCount(6, false)
//                    setGridDashedLine(DashPathEffect(floatArrayOf(5f, 5f), 0F))
//                    valueFormatter = object : ValueFormatter() {
//                        override fun getFormattedValue(value: Float): String = value.toLong().formatToDuration()
//                    }
//                }
//                data = BarData(dataset).apply { barWidth = 0.8f }
//            }
//            binding.barChart.invalidate()
//        } else {
//            binding.barChart.data = null
//            binding.barChart.invalidate()
//        }
//    }


    private fun initChartData(array: LongSparseArray<Long>) {
        if (array.isNotEmpty()) {
            val entries = mutableListOf<BarEntry>()
            val timeList = mutableListOf<Long>()

            for (index in 0 until array.size()) {
                timeList.add(array.keyAt(index))
                entries.add(BarEntry(index.toFloat(), array.valueAt(index).toFloat()))
            }

            val dataset = BarDataSet(entries, "AppScreenTimeChart").apply {
                setDrawValues(false)
                color = requireActivity().getColor(R.color.colorPrimary)
            }

            binding.barChart.apply {
                configureChart(this)
                configureXAxis(this, timeList, currentDateIndex)
                configureYAxis(this)
                data = BarData(dataset).apply { barWidth = 0.8f }
                invalidate()
            }
        } else {
            binding.barChart.data = null
            binding.barChart.invalidate()
        }
    }

    private fun configureChart(barChart: BarChart) {
        barChart.run {
            setScaleEnabled(false)
            setTouchEnabled(false)
            isDoubleTapToZoomEnabled = false
            setDrawGridBackground(false)
            setNoDataText("")
            setPadding(0, 0, 0, 0)
            setFitBars(true)
            description.isEnabled = false
            legend.isEnabled = false
        }
    }

    private fun configureXAxis(barChart: BarChart, timeList: List<Long>, currentDateIndex: Int) {
        barChart.xAxis.run {
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            mLabelHeight = 2
            textColor = requireActivity().getColor(R.color.color_999)
            axisLineColor = requireActivity().getColor(R.color.color_bcbcbc)
            granularity = 1.0f
            isGranularityEnabled = true
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value.toInt() < timeList.size) {
                        when (currentDateIndex) {
                            0, 1, 2 -> (timeList.getOrNull(value.toInt()) ?: 0L).formatTime("HH:mm")
                            3 -> (timeList.getOrNull(value.toInt()) ?: 0L).formatTime("MM.dd").removePrefix("0")
                            else -> ""
                        }
                    } else ""
                }
            }
        }
    }

    private fun configureYAxis(barChart: BarChart) {
        barChart.axisLeft.run {
            axisMinimum = 0f
            isEnabled = false
        }

        barChart.axisRight.run {
            setDrawZeroLine(false)
            setDrawLabels(false)
            axisLineColor = requireActivity().getColor(R.color.trans)
            zeroLineColor = requireActivity().getColor(R.color.trans)
            textColor = requireActivity().getColor(R.color.color_999)
            gridColor = requireActivity().getColor(R.color.color_bcbcbc)
            gridLineWidth = 0f
            axisMinimum = 0f
            setLabelCount(6, false)
            setGridDashedLine(DashPathEffect(floatArrayOf(5f, 5f), 0F))
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = value.toLong().formatToDuration()
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