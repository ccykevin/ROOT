package com.kevincheng.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextClock
import androidx.core.content.res.ResourcesCompat

class TextClock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : TextClock(context, attrs, defStyle) {

    init {
        init(attrs)
    }

    override fun onAttachedToWindow() {
        try {
            super.onAttachedToWindow()
        } catch (ignore: Exception) {
        }
    }

    private fun init(attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TextClock, 0, 0)
        try {
            attributes.getResourceId(R.styleable.TextClock_tc_font, -1).takeIf { it != -1 }
                ?.also {
                    typeface = ResourcesCompat.getFont(context, it)
                }
        } finally {
            attributes.recycle()
        }
    }
}