package com.kevincheng.widget

import android.widget.Checkable

interface IRadioButton : Checkable {
    val viewId: Int

    var value: Any?
    var listener: Listener?

    interface Listener {
        fun onCheckedChanged(radioButton: IRadioButton, isChecked: Boolean)
    }
}