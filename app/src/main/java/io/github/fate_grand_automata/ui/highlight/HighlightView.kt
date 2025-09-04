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
    val regionsToHighlight: Map<Region, HighlightItem>
) : View(Context) {
    private val rectPaints = HighlightColor.entries.associateWith {
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

    private val textPaints = HighlightColor.entries.associateWith {
        Paint().apply {
            color = when (it) {
                HighlightColor.Error -> Color.RED
                HighlightColor.Success -> Color.GREEN
                HighlightColor.Warning -> Color.YELLOW
                HighlightColor.Info -> Color.BLUE
            }
            textSize = 18f
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for ((region, item) in regionsToHighlight) {
            val rectPaint = rectPaints[item.color] ?: continue
            val textPaint = textPaints[item.color] ?: continue

            canvas.drawRect(
                region.x.toFloat(),
                region.y.toFloat(),
                region.right.toFloat(),
                region.bottom.toFloat(),
                rectPaint
            )

            item.text?.let {
                canvas.drawText(
                    it,
                    region.x.toFloat(),
                    region.y.toFloat() - 4,
                    textPaint
                )
            }
        }
    }
}