package com.kevincheng.ribsextensions.extensions

import com.uber.rib.core.Interactor
import com.uber.rib.core.InteractorBaseComponent
import com.uber.rib.core.Router
import com.uber.rib.core.screenstack.lifecycle.ScreenStackEvent

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
open class RouterExtended<I : Interactor<*, *>, C : InteractorBaseComponent<*>>(
    interactor: I,
    component: C
) : Router<I, C>(interactor, component) {

    private val attached = ArrayList<Router<*, *>>()

    open fun handleScreenEvents(router: Router<*, *>, event: ScreenStackEvent) {
        when (event) {
            ScreenStackEvent.APPEARED -> {
                if (attached.contains(router)) return
                router.also {
                    attachChild(it)
                    attached.add(it)
                }
            }
            ScreenStackEvent.REMOVED -> router.also {
                detachChild(it)
                attached.remove(it)
            }
            else -> {
            }
        }
    }
}