package com.clean.filecleaner.ui.module.clean.recent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.clean.filecleaner.databinding.FragmentScreenTimeBinding
import com.clean.filecleaner.ui.base.BaseFragment
import com.clean.filecleaner.ui.module.clean.recent.RecentAppHelper.getDateNameByIndex
import com.clean.filecleaner.ui.module.clean.recent.adapter.ScreenTimeAdapter
import com.clean.filecleaner.ui.module.clean.recent.viewmodel.ScreenTimeViewModel
import com.clean.filecleaner.ui.module.dialog.AppLaunchesFilterDialog

class TimesFragment : BaseFragment<FragmentScreenTimeBinding>() {

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentScreenTimeBinding {
        return FragmentScreenTimeBinding.inflate(layoutInflater)
    }


    private var currentDateIndex = 1
    private val viewModel by viewModels<ScreenTimeViewModel>()

    private val screenTimeAdapter by lazy {
        ScreenTimeAdapter(requireActivity()) {

        }
    }


    private fun setListeners() {
        binding.apply {
            btnDate.setOnClickListener {
                AppLaunchesFilterDialog(requireActivity()) { index, str ->
                    date.text = str
                    currentDateIndex = index
                }.show()
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        binding.date.text = getDateNameByIndex(currentDateIndex)
        setListeners()
    }


}