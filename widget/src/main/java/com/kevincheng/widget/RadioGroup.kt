package com.kevincheng.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes
import com.kevincheng.widget.rounded_corners.ClipViewStrategy
import com.kevincheng.widget.rounded_corners.IRoundedView

open class RadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), IRadioGroup, IRoundedView {

    final override val checkedButtonId: Int get() = checkedId
    final override val buttons: SparseArray<IRadioButton> = SparseArray()
    final override val buttonListener: IRadioButton.Listener = CheckedStateTracker()
    final override val hierarchyChangeListener: IRadioGroup.OnHierarchyChangeListener =
        PassThroughHierarchyChangeListener()

    final override var listener: IRadioGroup.Listener? = null

    private var checkedId = View.NO_ID
    private var lock = false
    private var isSubGroup: Boolean = false

    private lateinit var clipViewStrategy: ClipViewStrategy

    init {
        init(context, attrs)
        super.setOnHierarchyChangeListener(hierarchyChangeListener)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (checkedId != View.NO_ID) check(checkedId)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        clipViewStrategy = ClipViewStrategy.create(
            this,
            context,
            attrs,
            R.styleable.RadioGroup,
            R.styleable.RadioGroup_rc_radius
        )
        if (attrs == null) return
        val a = context.obtainStyledAttributes(attrs, R.styleable.RadioGroup, 0, 0)
        checkedId = a.getResourceId(R.styleable.RadioGroup_rg_checkedButton, View.NO_ID)
        a.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        clipViewStrategy.onLayout(changed, left, top, right, bottom)
    }

    override fun dispatchDraw(canvas: Canvas) {
        clipViewStrategy.beforeDispatchDraw(canvas)
        super.dispatchDraw(canvas)
        clipViewStrategy.afterDispatchDraw(canvas)
    }

    override fun draw(canvas: Canvas) {
        clipViewStrategy.beforeDispatchDraw(canvas)
        super.draw(canvas)
        clipViewStrategy.afterDispatchDraw(canvas)
    }

    override fun setCornerRadius(cornerRadius: Float) {
        clipViewStrategy.setCornerRadius(cornerRadius)
        invalidate()
    }

    override fun check(@IdRes id: Int) {
        setChecked(id)
    }

    override fun checkNoEvent(id: Int) {
        setChecked(id, changeEvent = false)
    }

    private fun setChecked(@IdRes id: Int, changeEvent: Boolean = true) {
        if (isSubGroup) return
        lock = true
        var previousCheckedButton: IRadioButton? = null
        buttons.get(checkedId)?.also {
            it.isChecked = false
            previousCheckedButton = it
        }
        var checkedButton: IRadioButton? = null
        buttons.get(id)?.also {
            it.isChecked = true
            checkedButton = it
        }
        checkedId = id
        lock = false
        if (changeEvent) listener?.onCheckedChanged(this, previousCheckedButton, checkedButton)
    }

    override fun asSubgroup(parentGroup: IRadioGroup) {
        super.setOnHierarchyChangeListener(parentGroup.hierarchyChangeListener)
        val tempKeys = arrayListOf<Int>()
        for (index in 0 until buttons.size()) {
            val key = buttons.keyAt(index)
            val button = buttons.get(key)
            if (button != null) {
                button.listener = parentGroup.buttonListener
                parentGroup.buttons.put(button.viewId, button)
                if (button.isChecked) parentGroup.check(button.viewId)
                tempKeys.add(key)
            }
        }
        tempKeys.forEach { buttons.remove(it) }
        isSubGroup = true
    }

    private inner class CheckedStateTracker : IRadioButton.Listener {

        override fun onCheckedChanged(radioButton: IRadioButton, isChecked: Boolean) {
            if (lock) return

            val id = when (isChecked) {
                true -> radioButton.viewId
                false -> View.NO_ID
            }
            check(id)
        }
    }

    private inner class PassThroughHierarchyChangeListener : IRadioGroup.OnHierarchyChangeListener {

        override fun onChildViewAdded(parent: View, child: View) {
            var id = child.id
            if (id == View.NO_ID) {
                id = View.generateViewId()
                child.id = id
            }

            (child as? IRadioGroup)?.also { it.asSubgroup(this@RadioGroup) }

            (child as? IRadioButton)?.also {
                it.listener = buttonListener
                buttons.put(it.viewId, it)
                if (it.isChecked) check(it.viewId)
            }
        }

        override fun onChildViewRemoved(parent: View, child: View) {
            (child as? IRadioButton)?.also {
                it.listener = null
                buttons.remove(it.viewId)
                if (checkedId == it.viewId) check(View.NO_ID)
            }
        }
    }
}