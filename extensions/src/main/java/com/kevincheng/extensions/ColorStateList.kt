package com.kevincheng.extensions

import android.content.res.ColorStateList
import androidx.annotation.ColorInt

fun colorStateListOf(vararg mapping: Pair<IntArray, Int>): ColorStateList {
    val (states, colors) = mapping.unzip()
    return ColorStateList(states.toTypedArray(), colors.toIntArray())
}

fun colorStateListOf(@ColorInt color: Int): ColorStateList {
    return ColorStateList.valueOf(color)
}