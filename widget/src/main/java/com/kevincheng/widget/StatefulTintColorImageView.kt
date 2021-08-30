package com.kevincheng.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class StatefulTintColorImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private var tint: ColorStateList? = null

    init {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs == null) return
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.StatefulTintColorImageView, 0, 0)
        tint =
            attributes.getColorStateList(R.styleable.StatefulTintColorImageView_stciv_tint)
        attributes.recycle()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateImageTint()
    }

    private fun updateImageTint() {
        val t = tint
        when {
            t != null -> setColorFilter(
                t.getColorForState(drawableState, t.defaultColor),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    fun setImageTint(color: ColorStateList?) {
        this.tint = color
        if (color != null)
            updateImageTint()
        else
            clearColorFilter()
    }
}