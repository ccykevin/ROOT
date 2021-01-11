package com.kevincheng.ribsextensions.extensions

import android.view.View
import com.uber.rib.core.Interactor
import com.uber.rib.core.InteractorBaseComponent
import com.uber.rib.core.Router
import com.uber.rib.core.ViewRouter
import com.uber.rib.core.screenstack.lifecycle.ScreenStackEvent

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
open class ViewRouterExtended<V : View, I : Interactor<*, *>, C : InteractorBaseComponent<*>>(
    view: V,
    interactor: I,
    component: C
) : ViewRouter<V, I, C>(view, interactor, component) {

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