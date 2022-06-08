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
    radiusId: Int,
    topLeftRadiusId: Int,
    topRightRadiusId: Int,
    bottomLeftRadiusId: Int,
    bottomRightRadiusId: Int
) {

    protected var radius: Float =
        DEFAULT_CORNER_RADIUS

    protected var topLeftRadius: Float =
        DEFAULT_CORNER_RADIUS

    protected var topRightRadius: Float =
        DEFAULT_CORNER_RADIUS

    protected var bottomLeftRadius: Float =
        DEFAULT_CORNER_RADIUS

    protected var bottomRightRadius: Float =
        DEFAULT_CORNER_RADIUS

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, attrs)
        radius = typedArray.getDimension(
            radiusId,
            DEFAULT_CORNER_RADIUS
        )
        topLeftRadius = typedArray.getDimension(
            topLeftRadiusId,
            DEFAULT_CORNER_RADIUS
        )
        topRightRadius = typedArray.getDimension(
            topRightRadiusId,
            DEFAULT_CORNER_RADIUS
        )
        bottomLeftRadius = typedArray.getDimension(
            bottomLeftRadiusId,
            DEFAULT_CORNER_RADIUS
        )
        bottomRightRadius = typedArray.getDimension(
            bottomRightRadiusId,
            DEFAULT_CORNER_RADIUS
        )
        if (radius != DEFAULT_CORNER_RADIUS) {
            topLeftRadius = radius
            topRightRadius = radius
            bottomLeftRadius = radius
            bottomRightRadius = radius
        }
        typedArray.recycle()
    }

    abstract fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int)

    abstract fun beforeDispatchDraw(canvas: Canvas)

    abstract fun afterDispatchDraw(canvas: Canvas)

    open fun setCornerRadius(cornerRadius: Float) {
        radius = cornerRadius
        topLeftRadius = cornerRadius
        topRightRadius = cornerRadius
        bottomLeftRadius = cornerRadius
        bottomRightRadius = cornerRadius
    }

    open fun setCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
        radius = DEFAULT_CORNER_RADIUS
        topLeftRadius = topLeft
        topRightRadius = topRight
        bottomLeftRadius = bottomLeft
        bottomRightRadius = bottomRight
    }

    companion object {
        const val DEFAULT_CORNER_RADIUS = 0f

        fun create(
            view: View,
            context: Context,
            attributeSet: AttributeSet?,
            attrs: IntArray,
            radiusId: Int,
            topLeftRadiusId: Int,
            topRightRadiusId: Int,
            bottomLeftRadiusId: Int,
            bottomRightRadiusId: Int
        ): ClipViewStrategy {
            return when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> ClipViewApi21Strategy(
                    view,
                    context,
                    attributeSet,
                    attrs,
                    radiusId,
                    topLeftRadiusId,
                    topRightRadiusId,
                    bottomLeftRadiusId,
                    bottomRightRadiusId
                )
                else -> ClipViewApi18Strategy(
                    view,
                    context,
                    attributeSet,
                    attrs,
                    radiusId,
                    topLeftRadiusId,
                    topRightRadiusId,
                    bottomLeftRadiusId,
                    bottomRightRadiusId
                )
            }
        }
    }
}