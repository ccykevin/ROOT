package com.kevincheng.widget

import android.util.SparseArray
import android.view.ViewGroup
import androidx.annotation.IdRes

interface IRadioGroup {
    val checkedButtonId: Int
    val buttons: SparseArray<IRadioButton>

    var buttonListener: IRadioButton.Listener
    var hierarchyChangeListener: OnHierarchyChangeListener
    var listener: Listener?

    fun check(@IdRes id: Int)
    fun checkNoEvent(@IdRes id: Int)
    fun asSubgroup(parentGroup: IRadioGroup)

    interface OnHierarchyChangeListener : ViewGroup.OnHierarchyChangeListener {
        var parentGroupListener: OnHierarchyChangeListener?
    }

    interface Listener {
        fun onCheckedChanged(radioGroup: IRadioGroup, checkedButton: IRadioButton?)
    }
}