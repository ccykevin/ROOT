package com.kevincheng.ribsextensions.extensions

import com.uber.rib.core.screenstack.lifecycle.ScreenStackEvent

interface ViewProviderView {
    fun handleScreenEvents(event: ScreenStackEvent)
}