package com.kevincheng.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

open class RadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle), IRadioButton {

    override val viewId: Int get() = id
    override var value: Any? = null
    override var listener: IRadioButton.Listener? = null

    private var checked: Boolean = false

    init {
        init(attrs)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    !isChecked && !isClickable && isEnabled -> {
                        toggle()
                        true
                    }
                    else -> super.onTouchEvent(event)
                }
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun init(attrs: AttributeSet?) {
        setOnClickListener {
            if (!isChecked) toggle()
        }
        isClickable = false
        if (attrs == null) return
        val a = context.obtainStyledAttributes(attrs, R.styleable.RadioButton, 0, 0)
        a.getString(R.styleable.RadioButton_rb_value)?.also { value = it }
        a.recycle()
    }

    override fun isChecked(): Boolean {
        return checked
    }

    override fun toggle() {
        isChecked = !checked
    }

    override fun setChecked(checked: Boolean) {
        if (this.checked == checked) return
        this.checked = checked
        listener?.onCheckedChanged(this, this.checked)
        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked) {
            View.mergeDrawableStates(drawableState, CheckedStateSet)
        }
        return drawableState
    }

    companion object {
        private val CheckedStateSet = intArrayOf(android.R.attr.state_checked)
    }
}