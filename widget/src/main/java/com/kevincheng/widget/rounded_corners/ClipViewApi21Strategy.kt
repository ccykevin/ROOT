package com.kevincheng.widget.rounded_corners

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider

internal class ClipViewApi21Strategy(
    view: View,
    context: Context,
    attributeSet: AttributeSet?,
    attrs: IntArray,
    attrIndex: Int
) : ClipViewStrategy(view, context, attributeSet, attrs, attrIndex) {

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun beforeDispatchDraw(canvas: Canvas) {
        view.clipToOutline = true
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun afterDispatchDraw(canvas: Canvas) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
    }
}