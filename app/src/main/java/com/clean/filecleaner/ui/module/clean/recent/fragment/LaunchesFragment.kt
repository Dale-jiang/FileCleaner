package com.clean.filecleaner.ui.module.clean.recent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.clean.filecleaner.databinding.FragmentLaunchesBinding
import com.clean.filecleaner.ui.base.BaseFragment

class LaunchesFragment:BaseFragment<FragmentLaunchesBinding>(){
    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLaunchesBinding {
        return FragmentLaunchesBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
    }

}