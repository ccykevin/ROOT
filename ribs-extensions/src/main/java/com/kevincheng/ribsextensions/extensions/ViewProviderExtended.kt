package com.kevincheng.ribsextensions.extensions

import com.uber.rib.core.ViewRouter
import com.uber.rib.core.screenstack.ViewProvider

abstract class ViewProviderExtended : ViewProvider() {

    var router: ViewRouter<*, *, *>? = null
        protected set

    override fun doOnViewRemoved() {
        router = null
    }
}