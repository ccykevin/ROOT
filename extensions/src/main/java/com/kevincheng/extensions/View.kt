package com.kevincheng.extensions

import android.app.Activity
import android.content.ContextWrapper
import android.view.View

fun View.activity(): Activity? {
    var unwrapContext = context
    while (unwrapContext is ContextWrapper) {
        if (unwrapContext is Activity) {
            return unwrapContext
        }
        unwrapContext = unwrapContext.baseContext
    }
    return null
}