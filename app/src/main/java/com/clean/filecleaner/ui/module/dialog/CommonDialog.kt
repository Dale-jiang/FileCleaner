package com.clean.filecleaner.ui.module.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.DialogCommonBinding

class CommonDialog(
    val title: String = "", val message: String = "", val leftBtn: String = "", val rightBtn: String = "",
    val cancelable: Boolean = true, val leftClick: Callback? = null, val rightClick: Callback? = null
) :
    DialogFragment() {

    private lateinit var binding: DialogCommonBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogCommonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = cancelable

        if (title.isEmpty()) {
            binding.title.text = context?.getString(R.string.app_name)
        } else {
            binding.title.text = title
        }

        if (message.isNotEmpty()) {
            binding.message.isVisible = true
            binding.message.text = message
        } else {
            binding.message.isVisible = false
        }

        if (leftBtn.isNotEmpty()) {
            binding.btnLeft.isVisible = true
            binding.btnLeft.text = leftBtn
        } else {
            binding.btnLeft.isVisible = false
        }

        if (rightBtn.isNotEmpty()) {
            binding.btnRight.isVisible = true
            binding.btnRight.text = rightBtn
        } else {
            binding.btnRight.isVisible = false
        }

        binding.btnLeft.setOnClickListener {
            dismiss()
            leftClick?.invoke()
        }

        binding.btnRight.setOnClickListener {
            dismiss()
            rightClick?.invoke()
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