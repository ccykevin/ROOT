package com.kevincheng.ribsextensions.extensions

import com.uber.rib.core.Interactor
import com.uber.rib.core.InteractorBaseComponent
import com.uber.rib.core.Router
import com.uber.rib.core.ViewRouter
import com.uber.rib.core.screenstack.lifecycle.ScreenStackEvent
import io.reactivex.disposables.CompositeDisposable

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
open class RouterExtended<I : Interactor<*, *>, C : InteractorBaseComponent<*>>(
    interactor: I,
    component: C
) : Router<I, C>(interactor, component) {

    protected val disposables = CompositeDisposable()
    protected val attachedViewProviderRouters = ArrayList<ViewRouter<*, *, *>>()

    override fun willDetach() {
        super.willDetach()
        disposables.clear()
        attachedViewProviderRouters.forEach { detachChild(it) }
        attachedViewProviderRouters.clear()
    }

    protected open fun viewProviderWillAttach(vararg viewProviders: ViewProviderExtended) {
        viewProviders.forEach { viewProvider ->
            disposables.add(viewProvider
                .lifecycle()
                .subscribe { event ->
                    handleScreenEvents(requireNotNull(viewProvider.router), event)
                }
            )
        }
    }

    protected open fun handleScreenEvents(router: ViewRouter<*, *, *>, event: ScreenStackEvent) {
        when (event) {
            ScreenStackEvent.APPEARED -> {
                if (!attachedViewProviderRouters.contains(router)) {
                    router.also {
                        attachChild(it)
                        attachedViewProviderRouters.add(it)
                    }
                }
            }
            ScreenStackEvent.REMOVED -> router.also {
                detachChild(it)
                attachedViewProviderRouters.remove(it)
            }
            else -> {
            }
        }
        (router.view as? ViewProviderView)?.handleScreenEvents(event)
    }
}