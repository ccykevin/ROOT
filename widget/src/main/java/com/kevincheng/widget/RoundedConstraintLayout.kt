package com.kevincheng.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.kevincheng.widget.rounded_corners.ClipViewStrategy
import com.kevincheng.widget.rounded_corners.IRoundedView

open class RoundedConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle),
    IRoundedView {

    private lateinit var clipViewStrategy: ClipViewStrategy

    init {
        initClipViewStrategy(context, attrs)
    }

    private fun initClipViewStrategy(context: Context, attrs: AttributeSet?) {
        clipViewStrategy = ClipViewStrategy.create(
            this,
            context,
            attrs,
            R.styleable.RoundedConstraintLayout,
            R.styleable.RoundedConstraintLayout_rc_radius,
            R.styleable.RoundedConstraintLayout_rc_topLeftRadius,
            R.styleable.RoundedConstraintLayout_rc_topRightRadius,
            R.styleable.RoundedConstraintLayout_rc_bottomLeftRadius,
            R.styleable.RoundedConstraintLayout_rc_bottomRightRadius
        )
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

    override fun draw(canvas: Canvas) {
        clipViewStrategy.beforeDispatchDraw(canvas)
        super.draw(canvas)
        clipViewStrategy.afterDispatchDraw(canvas)
    }

    override fun setCornerRadius(cornerRadius: Float) {
        clipViewStrategy.setCornerRadius(cornerRadius)
        invalidate()
    }

    override fun setCornerRadius(
        topLeft: Float,
        topRight: Float,
        bottomLeft: Float,
        bottomRight: Float
    ) {
        clipViewStrategy.setCornerRadius(topLeft, topRight, bottomLeft, bottomRight)
        invalidate()
    }
}