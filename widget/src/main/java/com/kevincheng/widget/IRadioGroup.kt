package com.kevincheng.widget

import android.util.SparseArray
import android.view.ViewGroup
import androidx.annotation.IdRes

interface IRadioGroup {
    val checkedButtonId: Int
    val buttons: SparseArray<IRadioButton>
    val buttonListener: IRadioButton.Listener
    val hierarchyChangeListener: OnHierarchyChangeListener

    var listener: Listener?

    fun check(@IdRes id: Int)
    fun checkNoEvent(@IdRes id: Int)
    fun asSubgroup(parentGroup: IRadioGroup)

    interface OnHierarchyChangeListener : ViewGroup.OnHierarchyChangeListener

    interface Listener {
        fun onCheckedChanged(radioGroup: IRadioGroup, previousCheckedButton: IRadioButton?, checkedButton: IRadioButton?)
    }
}