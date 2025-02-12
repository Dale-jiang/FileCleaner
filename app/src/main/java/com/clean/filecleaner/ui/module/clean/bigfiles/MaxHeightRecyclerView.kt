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

    private var maxHeight: Int = -1

    init {
//        attrs?.let { attributeSet ->
//            context.obtainStyledAttributes(attributeSet, R.styleable.MaxHeightRecyclerView).use { typedArray ->
//                maxHeight = typedArray.getLayoutDimension(R.styleable.MaxHeightRecyclerView_maxHeight, -1)
//            }
//        }

        val arr = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightRecyclerView)
        maxHeight = arr.getLayoutDimension(R.styleable.MaxHeightRecyclerView_maxHeight, maxHeight)
        arr.recycle()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val adjustedHeightSpec = if (maxHeight > 0) {
            val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
            val finalHeight = if (parentHeight > 0) minOf(parentHeight, maxHeight) else maxHeight
            MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.AT_MOST)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, adjustedHeightSpec)
    }
}