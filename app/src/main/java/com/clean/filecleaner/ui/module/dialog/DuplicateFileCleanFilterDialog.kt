package com.clean.filecleaner.ui.module.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.databinding.DialogDuplicateFileFilterBinding

class DuplicateFileCleanFilterDialog(val selectPosition: Int, val callBack: (Int) -> Unit) : DialogFragment() {

    private lateinit var binding: DialogDuplicateFileFilterBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogDuplicateFileFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        when (selectPosition) {
            0 -> {
                binding.keepLatest.setTextColor(requireActivity().getColor(R.color.colorPrimary))
                binding.keepOldest.setTextColor(requireActivity().getColor(R.color.color_text))
                binding.deselectAll.setTextColor(requireActivity().getColor(R.color.color_text))
            }
            1 -> {
                binding.keepLatest.setTextColor(requireActivity().getColor(R.color.color_text))
                binding.keepOldest.setTextColor(requireActivity().getColor(R.color.colorPrimary))
                binding.deselectAll.setTextColor(requireActivity().getColor(R.color.color_text))
            }
            2 -> {
                binding.keepLatest.setTextColor(requireActivity().getColor(R.color.color_text))
                binding.keepOldest.setTextColor(requireActivity().getColor(R.color.color_text))
                binding.deselectAll.setTextColor(requireActivity().getColor(R.color.colorPrimary))
            }
        }

        binding.keepLatest.setOnClickListener {
            dismiss()
            if (selectPosition != 0)
                callBack(0)

        }

        binding.keepOldest.setOnClickListener {
            dismiss()
            if (selectPosition != 1)
                callBack(1)
        }

        binding.deselectAll.setOnClickListener {
            dismiss()
            if (selectPosition != 2)
                callBack(2)
        }

    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.decorView?.background = null
        dialog?.window?.apply {
            setGravity(Gravity.CENTER)
            setLayout(ScreenUtils.getScreenWidth() - SizeUtils.dp2px(30f), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

}