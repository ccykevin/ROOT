package com.kevincheng.extensions

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

inline val Activity.view: View get() = window.decorView.rootView

fun Activity.clearCurrentFocus() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputMethodManager.isAcceptingText) {
        this.currentFocus?.apply {
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            clearFocus()
        }
    }
}