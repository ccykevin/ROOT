package com.kevincheng.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import java.util.concurrent.atomic.AtomicBoolean

open class AutoResizingTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyle) {

    private val updateSize = AtomicBoolean()
    private var prevBufferType: BufferType? = null
    private var prevTextSize: Int = 0

    override fun setText(text: CharSequence?, type: BufferType?) {
        var minTextSize = 0
        var maxTextSize = 0
        var granularity = 0
        val doHack =
            TextViewCompat.getAutoSizeTextType(this) != TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE && !(this.text == text && prevBufferType == type)
        prevBufferType = type
        if (doHack) {
            minTextSize = TextViewCompat.getAutoSizeMinTextSize(this)
            maxTextSize = TextViewCompat.getAutoSizeMaxTextSize(this)
            granularity = TextViewCompat.getAutoSizeStepGranularity(this)
            if (granularity < 0) granularity = 1
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                this,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
            )
            setTextSize(TypedValue.COMPLEX_UNIT_PX, maxTextSize.toFloat())
            measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
            )
            right = left
            bottom = top
            requestLayout()
        }
        super.setText(text, type)
        if (doHack) {
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                minTextSize,
                maxTextSize,
                granularity,
                TypedValue.COMPLEX_UNIT_PX
            )
        }
    }

    override fun onPreDraw(): Boolean {
        if (updateSize.compareAndSet(true, false)) {
            return super.onPreDraw()
        }
        val lp = layoutParams
        val newTextSize = textSize.toInt()
        if (lp != null && (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT || lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) &&
            TextViewCompat.getAutoSizeMaxTextSize(this) > newTextSize &&
            prevTextSize != newTextSize
        ) {
            prevTextSize = newTextSize
            updateSize.set(true)
            requestLayout()
        }
        return super.onPreDraw()
    }

    fun setNewAutoSizes(
        minTextSize: Int? = null,
        maxTextSize: Int? = null,
        granularity: Int? = null
    ) {
        val newMinTextSize = minTextSize ?: TextViewCompat.getAutoSizeMinTextSize(this)
        val newMaxTextSize = maxTextSize ?: TextViewCompat.getAutoSizeMaxTextSize(this)
        var newGranularity = granularity ?: TextViewCompat.getAutoSizeStepGranularity(this)
        if (newGranularity < 0) newGranularity = 1
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            this,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE
        )
        setTextSize(TypedValue.COMPLEX_UNIT_PX, newMaxTextSize.toFloat())
        measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
        )
        right = left
        bottom = top
        requestLayout()
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            this,
            newMinTextSize,
            newMaxTextSize,
            newGranularity,
            TypedValue.COMPLEX_UNIT_PX
        )
    }
}