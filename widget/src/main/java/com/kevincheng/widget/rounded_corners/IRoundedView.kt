package com.kevincheng.widget.rounded_corners

import android.graphics.Canvas

internal interface IRoundedView {
    fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int)
    fun dispatchDraw(canvas: Canvas)
    fun setCornerRadius(cornerRadius: Float)
}