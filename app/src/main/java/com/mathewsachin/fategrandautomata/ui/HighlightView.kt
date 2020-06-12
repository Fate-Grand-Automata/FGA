package com.mathewsachin.fategrandautomata.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.mathewsachin.libautomata.Region
import com.mathewsachin.fategrandautomata.util.AutomataApplication

private val regionsToHighlight = mutableSetOf<Region>()

val highlightView: View by lazy { HighlightView(AutomataApplication.Instance) }

fun addRegionToHighlight(Region: Region) {
    highlightView.post {
        regionsToHighlight.add(Region)

        highlightView.invalidate()
    }
}

fun removeRegionToHighlight(Region: Region) {
    highlightView.post {
        regionsToHighlight.remove(Region)

        highlightView.invalidate()
    }
}

private class HighlightView(Context: Context): View(Context) {
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (region in regionsToHighlight) {
            canvas?.drawRect(region.X.toFloat(),
                region.Y.toFloat(),
                region.right.toFloat(),
                region.bottom.toFloat(),
                paint)
        }
    }
}