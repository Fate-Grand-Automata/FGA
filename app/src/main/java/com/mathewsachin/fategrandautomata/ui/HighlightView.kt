package com.mathewsachin.fategrandautomata.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.mathewsachin.libautomata.Region

class HighlightView(
    Context: Context,
    val regionsToHighlight: Map<Region, Boolean>
) : View(Context) {
    private val red = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val green = Paint().apply {
        color = Color.GREEN
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for ((region, success) in regionsToHighlight) {
            canvas?.drawRect(
                region.X.toFloat(),
                region.Y.toFloat(),
                region.right.toFloat(),
                region.bottom.toFloat(),
                if (success) green else red
            )
        }
    }
}