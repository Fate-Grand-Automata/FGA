package com.mathewsachin.fategrandautomata.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.mathewsachin.libautomata.Region

class HighlightView(
    Context: Context,
    val regionsToHighlight: Set<Region>
) : View(Context) {
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (region in regionsToHighlight) {
            canvas?.drawRect(
                region.X.toFloat(),
                region.Y.toFloat(),
                region.right.toFloat(),
                region.bottom.toFloat(),
                paint
            )
        }
    }
}