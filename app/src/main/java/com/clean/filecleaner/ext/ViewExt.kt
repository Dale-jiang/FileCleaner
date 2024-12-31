package com.clean.filecleaner.ext

import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation

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