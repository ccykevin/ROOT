package com.kevincheng.extensions

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable

fun Drawable.backgroundColor(value: Int) {
    when (this) {
        is GradientDrawable -> setColor(value)
        is ShapeDrawable -> paint.color = value
        is ColorDrawable -> color = value
        else -> throw IllegalArgumentException("unsupported drawable type")
    }
}