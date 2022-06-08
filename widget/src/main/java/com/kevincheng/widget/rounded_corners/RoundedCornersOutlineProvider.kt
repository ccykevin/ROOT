package com.kevincheng.widget.rounded_corners

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RoundedCornersOutlineProvider(
    private val radius: Float? = null,
    private val topLeft: Float? = null,
    private val topRight: Float? = null,
    private val bottomLeft: Float? = null,
    private val bottomRight: Float? = null
) : ViewOutlineProvider() {

    private val topCorners = topLeft != null && topRight != null
    private val rightCorners = topRight != null && bottomRight != null
    private val bottomCorners = bottomLeft != null && bottomRight != null
    private val leftCorners = topLeft != null && bottomLeft != null
    private val topLeftCorner = topLeft != null
    private val topRightCorner = topRight != null
    private val bottomRightCorner = bottomRight != null
    private val bottomLeftCorner = bottomLeft != null

    override fun getOutline(view: View, outline: Outline) {
        val left = 0
        val top = 0
        val right = view.width
        val bottom = view.height

        if (radius != null) {
            outline.setRoundRect(left, top, right, bottom, radius)
        } else {
            val cornerRadius = when {
                topCorners -> max(topLeft ?: 0f, topRight ?: 0f)
                rightCorners -> max(topRight ?: 0f, bottomRight ?: 0f)
                bottomCorners -> max(bottomLeft ?: 0f, bottomRight ?: 0f)
                leftCorners -> max(topLeft ?: 0f, bottomLeft ?: 0f)
                topLeftCorner -> topLeft
                topRightCorner -> topRight
                bottomRightCorner -> bottomRight
                bottomLeftCorner -> bottomLeft
                else -> null
            } ?: 0f

            when {
                topCorners -> outline.setRoundRect(
                    left,
                    top,
                    right,
                    bottom + cornerRadius.toInt(),
                    cornerRadius
                )
                bottomCorners -> outline.setRoundRect(
                    left,
                    top - cornerRadius.toInt(),
                    right,
                    bottom,
                    cornerRadius
                )
                leftCorners -> outline.setRoundRect(
                    left,
                    top,
                    right + cornerRadius.toInt(),
                    bottom,
                    cornerRadius
                )
                rightCorners -> outline.setRoundRect(
                    left - cornerRadius.toInt(),
                    top,
                    right,
                    bottom,
                    cornerRadius
                )
                topLeftCorner -> outline.setRoundRect(
                    left,
                    top,
                    right + cornerRadius.toInt(),
                    bottom + cornerRadius.toInt(),
                    cornerRadius
                )
                bottomLeftCorner -> outline.setRoundRect(
                    left,
                    top - cornerRadius.toInt(),
                    right + cornerRadius.toInt(),
                    bottom,
                    cornerRadius
                )
                topRightCorner -> outline.setRoundRect(
                    left - cornerRadius.toInt(),
                    top,
                    right,
                    bottom + cornerRadius.toInt(),
                    cornerRadius
                )
                bottomRightCorner -> outline.setRoundRect(
                    left - cornerRadius.toInt(),
                    top - cornerRadius.toInt(),
                    right,
                    bottom,
                    cornerRadius
                )
            }
        }
    }
}