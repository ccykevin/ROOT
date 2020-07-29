package com.kevincheng.widget.rounded_corners

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

internal class ClipViewApi18Strategy(
    view: View,
    context: Context,
    attributeSet: AttributeSet?,
    attrs: IntArray,
    attrIndex: Int
) : ClipViewStrategy(view, context, attributeSet, attrs, attrIndex) {

    private val rectF: RectF = RectF()
    private val path: Path = Path()
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).also {
        it.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    private var savedCount: Int = -1

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        rectF.set(0f, 0f, view.width.toFloat(), view.height.toFloat())
        resetPath()
    }

    override fun beforeDispatchDraw(canvas: Canvas) {
        savedCount = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
    }

    override fun afterDispatchDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        canvas.restoreToCount(savedCount)
    }

    override fun setCornerRadius(cornerRadius: Float) {
        super.setCornerRadius(cornerRadius)
        resetPath()
    }

    private fun resetPath() {
        path.reset()
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW)
        path.close()
    }
}