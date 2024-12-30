package com.clean.filecleaner.ui.base

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.AdaptScreenUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
    private var timerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflateViewBinding()
        setContentView(binding.root)
        initView(savedInstanceState)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupImmersiveMode()
    }

    protected abstract fun setupImmersiveMode()
    protected abstract fun inflateViewBinding(): VB
    protected abstract fun initView(savedInstanceState: Bundle?)

    override fun getResources(): Resources {
        return AdaptScreenUtils.adaptHeight(super.getResources(), 760)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
    }


    protected fun startTimer(totalDuration: Long = 10_000L, checkInterval: Long = 200L, step: (Int, Job?) -> Unit, end: () -> Unit) {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            val maxIterations = (totalDuration / checkInterval).toInt()
            var currentValue = 0
            while (currentValue < maxIterations && isActive) {
                delay(checkInterval)
                currentValue += 1
                step(currentValue, timerJob)
                if (currentValue >= maxIterations) this.cancel()
            }
            if (currentValue >= maxIterations) end()
        }
    }

}