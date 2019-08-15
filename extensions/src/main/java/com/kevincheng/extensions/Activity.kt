package com.kevincheng.extensions

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun Activity.clearCurrentFocus() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputMethodManager.isAcceptingText) {
        this.currentFocus?.apply {
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            clearFocus()
        }
    }
}