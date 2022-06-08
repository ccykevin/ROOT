package com.kevincheng.widget.rounded_corners

internal interface IRoundedView {
    fun setCornerRadius(cornerRadius: Float)

    fun setCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float)
}