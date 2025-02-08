package com.clean.filecleaner.ui.module.clean.recent.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPageAdapter(activity: FragmentActivity, private val list: MutableList<Fragment>) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = list.size
    override fun createFragment(position: Int): Fragment = list[position]
}