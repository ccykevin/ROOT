package com.kevincheng.extensions

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlin.math.roundToInt

@ColorInt
fun adjustAlpha(@ColorInt color: Int, decimal: Float): Int {
    val alpha = (Color.alpha(color) * decimal).roundToInt()
    val red = Color.red(color)
    val green = Color.green(color)
    val blue = Color.blue(color)
    return Color.argb(alpha, red, green, blue)
}