package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.libautomata.CompareBy
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt

private sealed class ScaleBy(val rate: Double) {
    class Width(rate: Double) : ScaleBy(rate)
    class Height(rate: Double) : ScaleBy(rate)
}

private fun decideScaleMethod(OriginalSize: Size, DesiredSize: Size): ScaleBy {
    val rateToScaleByWidth = DesiredSize.Width / OriginalSize.Width.toDouble()
    val rateToScaleByHeight = DesiredSize.Height / OriginalSize.Height.toDouble()

    return if (rateToScaleByWidth <= rateToScaleByHeight)
        ScaleBy.Width(rateToScaleByWidth)
    else ScaleBy.Height(rateToScaleByHeight)
}

private fun calculateBorderThickness(Outer: Int, Inner: Int): Int {
    val size = abs(Outer - Inner)

    return (size / 2.0).roundToInt()
}

private fun calculateGameAreaWithoutBorders(
    ScriptSize: Size,
    ScreenSize: Size,
    ScaleRate: Double
): Region {
    val scaledScriptSize = ScriptSize * ScaleRate

    return Region(
        calculateBorderThickness(
            ScreenSize.Width,
            scaledScriptSize.Width
        ), // Offset(X)
        calculateBorderThickness(
            ScreenSize.Height,
            scaledScriptSize.Height
        ), // Offset(Y)
        scaledScriptSize.Width, // Game Width (without borders)
        scaledScriptSize.Height // Game Height (without borders)
    )
}

@ScriptScope
class Scaling @Inject constructor(val gameAreaManager: GameAreaManager, val game: Game) {
    private fun applyAspectRatioFix(ScriptSize: Size, ImageSize: Size) {
        val gameWithBorders = gameAreaManager.gameArea
        val scaleBy = decideScaleMethod(
            ScriptSize,
            gameWithBorders.size
        )
        val gameWithoutBorders =
            calculateGameAreaWithoutBorders(
                ScriptSize,
                gameWithBorders.size,
                scaleBy.rate
            )

        gameAreaManager.gameArea = gameWithoutBorders

        gameAreaManager.scriptDimension = when (scaleBy) {
            is ScaleBy.Width -> CompareBy.Width(ScriptSize.Width)
            is ScaleBy.Height -> CompareBy.Height(ScriptSize.Height)
        }

        gameAreaManager.compareDimension = when (scaleBy) {
            is ScaleBy.Width -> CompareBy.Width(ImageSize.Width)
            is ScaleBy.Height -> CompareBy.Height(ImageSize.Height)
        }
    }

    fun init() {
        applyAspectRatioFix(
            Game.scriptSize,
            Game.imageSize
        )
    }
}