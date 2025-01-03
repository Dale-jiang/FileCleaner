package com.clean.filecleaner.ext

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.widget.ProgressBar

fun View.startRotatingWithRotateAnimation(duration: Long = 1000L) {
    if (this.animation != null) {
        return
    }
    val rotateAnimation = RotateAnimation(
        0f, 360f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    ).apply {
        this.duration = duration
        this.repeatCount = Animation.INFINITE
        this.interpolator = LinearInterpolator()
    }

    this.startAnimation(rotateAnimation)
}

fun View.stopRotatingWithRotateAnimation() {
    this.clearAnimation()
}

fun ProgressBar.animateToProgressWithValueAnimator(targetProgress: Int, durationMillis: Long = 500L, onUpdate: ((animatedValue: Int) -> Unit)? = null) {
    val safeTarget = targetProgress.coerceIn(0, max)
    val animator = ValueAnimator.ofInt(0, safeTarget).apply {
        duration = durationMillis

        addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            this@animateToProgressWithValueAnimator.progress = animatedValue
            onUpdate?.invoke(animatedValue)
        }
    }
    animator.start()
}

fun View.startScaleAnimation() {
    this.startAnimation(
        ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).also {
            it.duration = 750
            it.repeatCount = Animation.INFINITE
            it.repeatMode = Animation.REVERSE
        }
    )
}