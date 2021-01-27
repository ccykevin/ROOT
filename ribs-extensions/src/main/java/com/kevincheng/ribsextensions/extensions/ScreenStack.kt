package com.kevincheng.ribsextensions.extensions

import android.view.View
import android.widget.FrameLayout
import androidx.annotation.UiThread
import com.jakewharton.rxrelay2.BehaviorRelay
import com.uber.rib.core.screenstack.ScreenStackBase
import com.uber.rib.core.screenstack.ViewProvider
import io.reactivex.Observable
import java.util.ArrayDeque

@UiThread
class ScreenStack(
    private val parentViewGroup: FrameLayout,
    private val singleView: Boolean = false
) : ScreenStackBase {

    private val backStack = ArrayDeque<ViewProviderExtended>()
    private val sizeRelay = BehaviorRelay.create<Int>().toSerialized()

    private var lastViewIndex: Int? = null

    val size: Observable<Int> get() = sizeRelay.hide()

    private val currentViewProvider: ViewProviderExtended?
        get() = when {
            backStack.isEmpty() -> null
            else -> backStack.peek()
        }

    override fun pushScreen(viewProvider: ViewProvider) {
        pushScreen(viewProvider, false)
    }

    override fun pushScreen(viewProvider: ViewProvider, shouldAnimate: Boolean) {
        if (viewProvider !is ViewProviderExtended) throw IllegalStateException("viewProvider must be the class of ViewProviderExtended")
        when (singleView) {
            true -> onCurrentViewRemoved()
            false -> onCurrentViewHidden()
        }
        removeCurrentView()
        backStack.push(viewProvider)
        sizeRelay.accept(backStack.size)
        buildCurrentView()
        onCurrentViewAppeared()
        addCurrentView()
    }

    override fun popScreen() {
        popScreen(false)
    }

    override fun popScreen(shouldAnimate: Boolean) {
        if (backStack.isEmpty()) return
        backStack.pop().also {
            onCurrentViewRemoved(it)
            removeCurrentView(it)
        }
        sizeRelay.accept(backStack.size)
        buildCurrentView()
        onCurrentViewAppeared()
        addCurrentView()
    }

    override fun popBackTo(index: Int, shouldAnimate: Boolean) {
        for (size in backStack.size - 1 downTo index + 1) {
            popScreen()
        }
    }

    override fun handleBackPress(): Boolean {
        return handleBackPress(false)
    }

    override fun handleBackPress(shouldAnimate: Boolean): Boolean {
        if (backStack.size == 1) {
            return false
        }
        popScreen()
        return true
    }

    override fun size(): Int {
        return backStack.size
    }

    /**
     * Returns the index of the last item in the stack.
     * @return -1 is return when the backstack is empty.
     */
    fun indexOfLastItem(): Int {
        return size() - 1
    }

    private fun onCurrentViewHidden() {
        currentViewProvider?.onViewHidden()
    }

    private fun onCurrentViewAppeared() {
        currentViewProvider?.onViewAppeared()
    }

    private fun onCurrentViewRemoved(viewProvider: ViewProviderExtended? = null) {
        val vp = viewProvider ?: currentViewProvider
        vp?.onViewRemoved()
    }

    private fun buildCurrentView() {
        currentViewProvider?.also {
            if (it.router == null) it.buildView(parentViewGroup)
        }
    }

    private fun addCurrentView() {
        var index = lastViewIndex ?: -1

        if (!singleView && index != -1) index += 1

        currentViewProvider?.router?.also {
            when (parentViewGroup.indexOfChild(it.view) == -1) {
                true -> parentViewGroup.addView(it.view, index)
                false -> it.view.visibility = View.VISIBLE
            }
        }
    }

    private fun removeCurrentView(viewProvider: ViewProviderExtended? = null) {
        lastViewIndex = null

        val vp = viewProvider ?: currentViewProvider

        vp?.router?.also {
            lastViewIndex = parentViewGroup.indexOfChild(it.view)

            val pushScreen = viewProvider == null
            when {
                pushScreen && !singleView -> it.view.visibility = View.GONE
                else -> parentViewGroup.removeView(it.view)
            }
        }
    }
}