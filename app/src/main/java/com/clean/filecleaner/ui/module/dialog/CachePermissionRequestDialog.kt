package com.clean.filecleaner.ui.module.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.databinding.DialogRequestCachePermissionBinding

class CachePermissionRequestDialog(val ok: Callback) : DialogFragment() {

    private lateinit var binding: DialogRequestCachePermissionBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogRequestCachePermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true


        binding.btnGrant.setOnClickListener {
            dismiss()
            ok()
        }

        binding.btnNotNow.setOnClickListener {
            dismiss()
        }

    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.decorView?.background = null
        dialog?.window?.apply {
            setGravity(Gravity.CENTER)
            setLayout(ScreenUtils.getScreenWidth() - SizeUtils.dp2px(20f), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

}