package io.github.fate_grand_automata.ui.highlight

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import io.github.lib_automata.HighlightColor
import io.github.lib_automata.Region

@SuppressLint("ViewConstructor")
class HighlightView(
    Context: Context,
    val regionsToHighlight: Map<Region, HighlightColor>
) : View(Context) {
    private val colors = HighlightColor.entries.associateWith {
        Paint().apply {
            color = when (it) {
                HighlightColor.Error -> Color.RED
                HighlightColor.Success -> Color.GREEN
                HighlightColor.Warning -> Color.YELLOW
                HighlightColor.Info -> Color.BLUE
            }
            strokeWidth = 5f
            style = Paint.Style.STROKE
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for ((region, color) in regionsToHighlight) {
            canvas.drawRect(
                region.x.toFloat(),
                region.y.toFloat(),
                region.right.toFloat(),
                region.bottom.toFloat(),
                colors[color] ?: continue
            )
        }
    }
}