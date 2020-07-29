package com.kevincheng.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import com.kevincheng.widget.rounded_corners.ClipViewStrategy
import com.kevincheng.widget.rounded_corners.IRoundedView

open class RoundedFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle),
    IRoundedView {

    private lateinit var clipViewStrategy: ClipViewStrategy

    init {
        initClipViewStrategy(context, attrs)
    }

    private fun initClipViewStrategy(context: Context, attrs: AttributeSet?) {
        clipViewStrategy = ClipViewStrategy.create(this, context, attrs, R.styleable.RoundedFrameLayout, R.styleable.RoundedFrameLayout_rc_radius)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        clipViewStrategy.onLayout(changed, left, top, right, bottom)
    }

    override fun dispatchDraw(canvas: Canvas) {
        clipViewStrategy.beforeDispatchDraw(canvas)
        super.dispatchDraw(canvas)
        clipViewStrategy.afterDispatchDraw(canvas)
    }

    override fun setCornerRadius(cornerRadius: Float) {
        clipViewStrategy.setCornerRadius(cornerRadius)
        invalidate()
    }
}