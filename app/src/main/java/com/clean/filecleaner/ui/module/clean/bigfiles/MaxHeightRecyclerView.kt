package com.clean.filecleaner.ui.module.clean.bigfiles

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.clean.filecleaner.R

class MaxHeightRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    // 当 maxHeight 小于等于 0 时，表示不限制高度
    private var maxHeight: Int = -1

    init {
        // 如果属性不为 null，则读取自定义属性（使用 Kotlin 扩展函数自动回收 TypedArray）
        attrs?.let { attributeSet ->
            context.obtainStyledAttributes(attributeSet, R.styleable.MaxHeightRecyclerView).use { typedArray ->
                maxHeight = typedArray.getLayoutDimension(R.styleable.MaxHeightRecyclerView_maxHeight, -1)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val adjustedHeightSpec = if (maxHeight > 0) {
            // 获取父容器传入的高度限制
            val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
            // 如果父容器有约束，则取父容器高度和自定义 maxHeight 的较小值，
            // 如果父容器没有约束（UNSPECIFIED），则使用自定义的 maxHeight
            val finalHeight = if (parentHeight > 0) minOf(parentHeight, maxHeight) else maxHeight
            // 使用 AT_MOST 模式，确保高度不会超过 finalHeight
            MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.AT_MOST)
        } else {
            // 若未设置 maxHeight，则保持原有测量规格
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, adjustedHeightSpec)
    }
}