package com.kevincheng.widget.rounded_corners

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View

internal abstract class ClipViewStrategy(
    val view: View,
    context: Context,
    attributeSet: AttributeSet?,
    attrs: IntArray,
    attrIndex: Int
) {

    var radius: Float =
        DEFAULT_CORNER_RADIUS

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, attrs)
        radius = typedArray.getDimension(attrIndex,
            DEFAULT_CORNER_RADIUS
        )
        typedArray.recycle()
    }

    abstract fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int)

    abstract fun beforeDispatchDraw(canvas: Canvas)

    abstract fun afterDispatchDraw(canvas: Canvas)

    open fun setCornerRadius(cornerRadius: Float) {
        radius = cornerRadius
    }

    companion object {
        private const val DEFAULT_CORNER_RADIUS = 0f

        fun create(
            view: View,
            context: Context,
            attributeSet: AttributeSet?,
            attrs: IntArray,
            attrIndex: Int
        ): ClipViewStrategy {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> ClipViewApi21Strategy(
                    view,
                    context,
                    attributeSet,
                    attrs,
                    attrIndex
                )
                else -> ClipViewApi18Strategy(
                    view,
                    context,
                    attributeSet,
                    attrs,
                    attrIndex
                )
            }
        }
    }
}