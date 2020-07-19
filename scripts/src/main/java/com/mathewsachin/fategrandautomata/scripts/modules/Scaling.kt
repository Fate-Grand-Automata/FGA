package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.libautomata.CompareSettings
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Size
import kotlin.math.abs
import kotlin.math.roundToInt

private data class ScalingMethod(val ByWidth: Boolean, val Rate: Double)

private fun decideScaleMethod(OriginalSize: Size, DesiredSize: Size): ScalingMethod {
    val rateToScaleByWidth = DesiredSize.Width / OriginalSize.Width.toDouble()
    val rateToScaleByHeight = DesiredSize.Height / OriginalSize.Height.toDouble()

    return if (rateToScaleByWidth <= rateToScaleByHeight)
        ScalingMethod(
            true,
            rateToScaleByWidth
        )
    else ScalingMethod(
        false,
        rateToScaleByHeight
    )
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

private fun applyAspectRatioFix(ScriptSize: Size, ImageSize: Size) {
    val gameWithBorders = GameAreaManager.GameArea
    val (scaleByWidth, scaleRate) = decideScaleMethod(
        ScriptSize,
        gameWithBorders.size
    )
    val gameWithoutBorders =
        calculateGameAreaWithoutBorders(
            ScriptSize,
            gameWithBorders.size,
            scaleRate
        )

    GameAreaManager.GameArea = gameWithoutBorders

    if (scaleByWidth) {
        GameAreaManager.ScriptDimension = CompareSettings(true, ScriptSize.Width)
        GameAreaManager.CompareDimension = CompareSettings(true, ImageSize.Width)
    } else {
        GameAreaManager.ScriptDimension = CompareSettings(false, ScriptSize.Height)
        GameAreaManager.CompareDimension = CompareSettings(false, ImageSize.Height)
    }
}

fun initScaling() {
    GameAreaManager.reset()

    applyAspectRatioFix(
        Game.ScriptSize,
        Game.ImageSize
    )
}