package com.clean.filecleaner.ui.module.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.DialogAppLaunchFilterBinding
import com.clean.filecleaner.ui.module.clean.recent.RecentAppHelper.getDateNameByIndex
import com.google.android.material.bottomsheet.BottomSheetDialog

class AppLaunchesFilterDialog(val context: Activity, val callback: (value: Int, text: String) -> Unit) : BottomSheetDialog(context, R.style.CustomBottomSheetDialog) {

    private lateinit var binding: DialogAppLaunchFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAppLaunchFilterBinding.inflate(LayoutInflater.from(context), context.window.decorView as ViewGroup, false)
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {
        binding.apply {

            val last60minStr = getDateNameByIndex(0)
            val todayStr = getDateNameByIndex(1)
            val yesterdayStr = getDateNameByIndex(2)
            val last7daysStr = getDateNameByIndex(3)

            btnLast60min.text = last60minStr
            btnToday.text = todayStr
            btnYesterday.text = yesterdayStr
            btnLast7days.text = last7daysStr

            btnLast60min.setOnClickListener {
                callback(0, last60minStr)
                dismiss()
            }
            btnToday.setOnClickListener {
                callback(1, todayStr)
                dismiss()
            }
            btnYesterday.setOnClickListener {
                callback(2, yesterdayStr)
                dismiss()
            }
            btnLast7days.setOnClickListener {
                callback(3, last7daysStr)
                dismiss()
            }

        }
    }

    override fun onStart() {
        super.onStart()
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

}