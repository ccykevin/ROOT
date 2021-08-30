package com.kevincheng.widget

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import com.google.android.flexbox.FlexboxLayout

open class FlexboxRadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FlexboxLayout(context, attrs, defStyle), IRadioGroup {

    final override val checkedButtonId: Int get() = checkedId
    final override val buttons: SparseArray<IRadioButton> = SparseArray()
    final override var buttonListener: IRadioButton.Listener = CheckedStateTracker()
    final override var hierarchyChangeListener: IRadioGroup.OnHierarchyChangeListener =
        PassThroughHierarchyChangeListener()
    final override var listener: IRadioGroup.Listener? = null

    private var checkedId = View.NO_ID
    private var lock = false
    private var isSubGroup: Boolean = false

    init {
        super.setOnHierarchyChangeListener(hierarchyChangeListener)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (checkedId != View.NO_ID) check(checkedId)
    }

    final override fun check(@IdRes id: Int) {
        setChecked(id)
    }

    override fun checkNoEvent(id: Int) {
        setChecked(id, changeEvent = false)
    }

    private fun setChecked(@IdRes id: Int, changeEvent: Boolean = true) {
        if (isSubGroup) return
        lock = true
        buttons.get(checkedId)?.isChecked = false
        var checkedButton: IRadioButton? = null
        buttons.get(id)?.also {
            it.isChecked = true
            checkedButton = it
        }
        checkedId = id
        lock = false
        if (changeEvent) listener?.onCheckedChanged(this, checkedButton)
    }

    final override fun asSubgroup(parentGroup: IRadioGroup) {
        hierarchyChangeListener.parentGroupListener = parentGroup.hierarchyChangeListener
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
        override var parentGroupListener: IRadioGroup.OnHierarchyChangeListener? = null

        override fun onChildViewAdded(parent: View, child: View) {
            when {
                parentGroupListener != null -> parentGroupListener?.onChildViewAdded(parent, child)
                else -> {
                    var id = child.id
                    if (id == View.NO_ID) {
                        id = View.generateViewId()
                        child.id = id
                    }

                    (child as? IRadioGroup)?.also { it.asSubgroup(this@FlexboxRadioGroup) }

                    (child as? IRadioButton)?.also {
                        it.listener = buttonListener
                        buttons.put(it.viewId, it)
                        if (it.isChecked) check(it.viewId)
                    }
                }
            }
        }

        override fun onChildViewRemoved(parent: View, child: View) {
            when {
                parentGroupListener != null -> parentGroupListener?.onChildViewRemoved(
                    parent,
                    child
                )
                else -> (child as? IRadioButton)?.also {
                    it.listener = null
                    buttons.remove(it.viewId)
                    if (checkedId == it.viewId) check(View.NO_ID)
                }
            }
        }
    }
}