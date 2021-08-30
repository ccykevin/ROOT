package com.kevincheng.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.IdRes

class RadioGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), IRadioGroup {

    override val checkedButtonId: Int get() = checkedId
    override val buttons: SparseArray<IRadioButton> = SparseArray()
    override var buttonListener: IRadioButton.Listener = CheckedStateTracker()
    override var hierarchyChangeListener: IRadioGroup.OnHierarchyChangeListener =
        PassThroughHierarchyChangeListener()
    override var listener: IRadioGroup.Listener? = null

    private var checkedId = View.NO_ID
    private var lock = false
    private var isSubGroup: Boolean = false

    private var cornerRadius: Float = 0f
    private lateinit var rectF: RectF
    private lateinit var path: Path
    private lateinit var paint: Paint

    private val roundCorners: Boolean get() = cornerRadius > 0f

    init {
        init(attrs)
        if (roundCorners) {
            path = Path()
            paint = Paint().apply {
                isAntiAlias = true
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
        }
        super.setOnHierarchyChangeListener(hierarchyChangeListener)
    }

    private fun init(attrs: AttributeSet?) {
        if (attrs == null) return
        val a = context.obtainStyledAttributes(attrs, R.styleable.RadioGroup, 0, 0)
        checkedId = a.getResourceId(R.styleable.RadioGroup_rg_checkedButton, View.NO_ID)
        cornerRadius = a.getDimension(R.styleable.RadioGroup_rg_cornerRadius, 0f)
        a.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (checkedId != View.NO_ID) check(checkedId)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!roundCorners) return
        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
        resetPath()
    }

    override fun draw(canvas: Canvas) {
        when (roundCorners) {
            true -> {
                val sc = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> canvas.saveLayer(
                        rectF,
                        null
                    )
                    else -> canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
                }
                super.draw(canvas)
                canvas.drawPath(path, paint)
                canvas.restoreToCount(sc)
            }
            false -> super.draw(canvas)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        when (roundCorners) {
            true -> {
                val sc = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> canvas.saveLayer(
                        rectF,
                        null
                    )
                    else -> canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
                }
                super.dispatchDraw(canvas)
                canvas.drawPath(path, paint)
                canvas.restoreToCount(sc)
            }
            false -> super.dispatchDraw(canvas)
        }
    }

    private fun resetPath() {
        path.reset()
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        path.close()
    }

    override fun check(@IdRes id: Int) {
        setChecked(id)
    }

    override fun checkNoEvent(id: Int) {
        setChecked(id, changeEvent = false)
    }

    private fun setChecked(@IdRes id: Int, changeEvent: Boolean = true) {
        if (isSubGroup) return
        lock = true
        buttons.get(checkedId)?.isChecked = false
        var checkedButton: IRadioButton? = null
        buttons.get(id)?.also {
            it.isChecked = true
            checkedButton = it
        }
        checkedId = id
        lock = false
        if (changeEvent) listener?.onCheckedChanged(this, checkedButton)
    }

    override fun asSubgroup(parentGroup: IRadioGroup) {
        hierarchyChangeListener.parentGroupListener = parentGroup.hierarchyChangeListener
        val tempKeys = arrayListOf<Int>()
        for (index in 0 until buttons.size()) {
            val key = buttons.keyAt(index)
            val button = buttons.get(key)
            if (button != null) {
                button.listener = parentGroup.buttonListener
                parentGroup.buttons.put(button.viewId, button)
                if (button.isChecked) parentGroup.check(button.viewId)
                tempKeys.add(key)
            }
        }
        tempKeys.forEach { buttons.remove(it) }
        isSubGroup = true
    }

    private inner class CheckedStateTracker : IRadioButton.Listener {
        override fun onCheckedChanged(radioButton: IRadioButton, isChecked: Boolean) {
            if (lock) return

            val id = when (isChecked) {
                true -> radioButton.viewId
                false -> View.NO_ID
            }
            check(id)
        }
    }

    private inner class PassThroughHierarchyChangeListener : IRadioGroup.OnHierarchyChangeListener {
        override var parentGroupListener: IRadioGroup.OnHierarchyChangeListener? = null

        override fun onChildViewAdded(parent: View, child: View) {
            when {
                parentGroupListener != null -> parentGroupListener?.onChildViewAdded(parent, child)
                else -> {
                    var id = child.id
                    if (id == View.NO_ID) {
                        id = View.generateViewId()
                        child.id = id
                    }

                    (child as? IRadioGroup)?.also { it.asSubgroup(this@RadioGroup) }

                    (child as? IRadioButton)?.also {
                        it.listener = buttonListener
                        buttons.put(it.viewId, it)
                        if (it.isChecked) check(it.viewId)
                    }
                }
            }
        }

        override fun onChildViewRemoved(parent: View, child: View) {
            when {
                parentGroupListener != null -> parentGroupListener?.onChildViewRemoved(
                    parent,
                    child
                )
                else -> (child as? IRadioButton)?.also {
                    it.listener = null
                    buttons.remove(it.viewId)
                    if (checkedId == it.viewId) check(View.NO_ID)
                }
            }
        }
    }
}