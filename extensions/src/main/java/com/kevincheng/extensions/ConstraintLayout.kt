package com.kevincheng.extensions

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

fun ConstraintLayout.addViewAndConstraints(
    view: View,
    layoutParams: ConstraintLayout.LayoutParams? = null,
    block: KotlinConstraintSet.() -> Unit
): KotlinConstraintSet {
    if (view.id == -1) view.id = View.generateViewId()
    layoutParams?.run { view.layoutParams = this }
    if (view.layoutParams == null || view.layoutParams !is ConstraintLayout.LayoutParams) view.layoutParams = ConstraintLayout.LayoutParams(0, 0)
    addView(view)
    return addConstraints(block)
}

fun ConstraintLayout.addConstraints(block: KotlinConstraintSet.() -> Unit): KotlinConstraintSet {
    return newConstraintSet(block)
        .also { it.applyTo(this) }
}

fun ConstraintLayout.newConstraintSet(block: KotlinConstraintSet.() -> Unit): KotlinConstraintSet {
    return newConstraintSet()
        .apply(block)
}

fun ConstraintLayout.newConstraintSet(): KotlinConstraintSet {
    return KotlinConstraintSet()
        .also { it.clone(this) }
}

class KotlinConstraintSet : ConstraintSet() {

    private var margin: Int? = null
        get() {
            val result = field
            margin = null
            return result
        }

    fun setMargin(margin: Int) {
        this.margin = margin
    }

    infix fun Unit.to(other: View) = other

    infix fun View.topToTopOf(target: View) {
        val targetId = when (this.parent) {
            target -> PARENT_ID
            else -> target.id
        }
        margin?.also {
            connect(id, TOP, targetId, TOP, it)
        } ?: connect(id, TOP, targetId, TOP)
    }

    infix fun View.topToBottomOf(target: View) {
        val targetId = when (this.parent) {
            target -> PARENT_ID
            else -> target.id
        }
        margin?.also {
            connect(id, TOP, targetId, BOTTOM, it)
        } ?: connect(id, TOP, targetId, BOTTOM)
    }

    infix fun View.bottomToBottomOf(target: View) {
        val targetId = when (this.parent) {
            target -> PARENT_ID
            else -> target.id
        }
        margin?.also {
            connect(id, BOTTOM, targetId, BOTTOM, it)
        } ?: connect(id, BOTTOM, targetId, BOTTOM)
    }

    infix fun View.bottomToTopOf(target: View) {
        val targetId = when (this.parent) {
            target -> PARENT_ID
            else -> target.id
        }
        margin?.also {
            connect(id, BOTTOM, targetId, TOP, it)
        } ?: connect(id, BOTTOM, targetId, TOP)
    }

    infix fun View.leftToLeftOf(target: View) {
        val targetId = when (this.parent) {
            target -> PARENT_ID
            else -> target.id
        }
        margin?.also {
            connect(id, LEFT, targetId, LEFT, it)
        } ?: connect(id, LEFT, targetId, LEFT)
    }

    infix fun View.leftToRightOf(target: View) {
        val targetId = when (this.parent) {
            target -> PARENT_ID
            else -> target.id
        }
        margin?.also {
            connect(id, LEFT, targetId, RIGHT, it)
        } ?: connect(id, LEFT, targetId, RIGHT)
    }

    infix fun View.rightToLeftOf(target: View) {
        val targetId = when (this.parent) {
            target -> PARENT_ID
            else -> target.id
        }
        margin?.also {
            connect(id, RIGHT, targetId, LEFT, it)
        } ?: connect(id, RIGHT, targetId, LEFT)
    }

    infix fun View.rightToRightOf(target: View) {
        val targetId = when (this.parent) {
            target -> PARENT_ID
            else -> target.id
        }
        margin?.also {
            connect(id, RIGHT, targetId, RIGHT, it)
        } ?: connect(id, RIGHT, targetId, RIGHT)
    }

    infix fun View.clear(constraint: Constraints) =
        when (constraint) {
            Constraints.TOP -> clear(this.id, TOP)
            Constraints.BOTTOM -> clear(this.id, BOTTOM)
            Constraints.LEFT -> clear(this.id, LEFT)
            Constraints.RIGHT -> clear(this.id, RIGHT)
            Constraints.START -> clear(this.id, START)
            Constraints.END -> clear(this.id, END)
        }

    infix fun clones(constraintLayout: ConstraintLayout) = clone(constraintLayout)

    inline fun constraint(view: View, block: View.() -> Unit) = view.apply(block)

    infix fun appliesTo(constraintLayout: ConstraintLayout) = applyTo(constraintLayout)
}

enum class Constraints {
    TOP, BOTTOM, LEFT, RIGHT, START, END
}