package com.kevincheng.widget.rounded_corners

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View

internal class ClipViewApi21Strategy(
    view: View,
    context: Context,
    attributeSet: AttributeSet?,
    attrs: IntArray,
    radiusId: Int,
    topLeftRadiusId: Int,
    topRightRadiusId: Int,
    bottomLeftRadiusId: Int,
    bottomRightRadiusId: Int
) : ClipViewStrategy(
    view,
    context,
    attributeSet,
    attrs,
    radiusId,
    topLeftRadiusId,
    topRightRadiusId,
    bottomLeftRadiusId,
    bottomRightRadiusId
) {

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun beforeDispatchDraw(canvas: Canvas) {
        view.clipToOutline = true
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun afterDispatchDraw(canvas: Canvas) {
        view.outlineProvider =
            RoundedCornersOutlineProvider(
                radius.takeIf { it != DEFAULT_CORNER_RADIUS },
                topLeftRadius.takeIf { it != DEFAULT_CORNER_RADIUS },
                topRightRadius.takeIf { it != DEFAULT_CORNER_RADIUS },
                bottomLeftRadius.takeIf { it != DEFAULT_CORNER_RADIUS },
                bottomRightRadius.takeIf { it != DEFAULT_CORNER_RADIUS })
    }
}