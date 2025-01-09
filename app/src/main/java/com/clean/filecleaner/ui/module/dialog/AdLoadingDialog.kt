package com.clean.filecleaner.ui.module.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.clean.filecleaner.databinding.DialogAdLoadingBinding
import com.clean.filecleaner.ext.startRotatingWithRotateAnimation
import com.clean.filecleaner.ext.stopRotatingWithRotateAnimation

class AdLoadingDialog : DialogFragment() {

    private lateinit var binding: DialogAdLoadingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogAdLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.loading.startRotatingWithRotateAnimation()
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.decorView?.background = null
        dialog?.window?.apply {
            setGravity(Gravity.CENTER)
            setLayout(ScreenUtils.getScreenWidth() - SizeUtils.dp2px(30f), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        kotlin.runCatching {
            binding.loading.stopRotatingWithRotateAnimation()
        }
    }

}