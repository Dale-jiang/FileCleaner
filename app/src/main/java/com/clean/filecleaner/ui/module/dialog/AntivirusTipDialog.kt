package com.clean.filecleaner.ui.module.dialog

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.clean.filecleaner.R
import com.clean.filecleaner.data.Callback
import com.clean.filecleaner.data.WEB_URL
import com.clean.filecleaner.databinding.DialogAntivirusTipBinding
import com.clean.filecleaner.ui.module.WebActivity

class AntivirusTipDialog(val ok: Callback) : DialogFragment() {

    private lateinit var binding: DialogAntivirusTipBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogAntivirusTipBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        val spanned = Html.fromHtml(getString(R.string.antivirus_tip), Html.FROM_HTML_MODE_COMPACT)
        val spannable = SpannableString(spanned)

        runCatching {

            val spans = spannable.getSpans(0, spanned.length, ClickableSpan::class.java)
            for (span in spans) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                val flags = spannable.getSpanFlags(span)
                spannable.removeSpan(span)
                spannable.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        startActivity(Intent(context, WebActivity::class.java).apply {
                            putExtra(WEB_URL, "https://www.trustlook.com/privacy-policy")
                        })
                    }
                }, start, end, flags)
            }

            binding.des.text = spannable
            binding.des.movementMethod = LinkMovementMethod.getInstance()
        }.onFailure {
            binding.des.text = spannable
        }

        binding.btnAgree.setOnClickListener {
            dismiss()
            ok()
        }

        binding.ivClose.setOnClickListener {
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