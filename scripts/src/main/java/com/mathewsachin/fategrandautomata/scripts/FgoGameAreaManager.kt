package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private sealed class ScaleBy(val rate: Double) : Comparable<ScaleBy> {
    class Width(rate: Double) : ScaleBy(rate)
    class Height(rate: Double) : ScaleBy(rate)

    override fun compareTo(other: ScaleBy) =
        rate.compareTo(other.rate)
}

private fun decideScaleMethod(originalSize: Size, desiredSize: Size) =
    minOf(
        ScaleBy.Width(desiredSize.width / originalSize.width.toDouble()),
        ScaleBy.Height(desiredSize.height / originalSize.height.toDouble())
    )

private fun calculateBorderThickness(outer: Int, inner: Int) =
    ((outer - inner).absoluteValue / 2.0).roundToInt()

private fun calculateGameAreaWithoutBorders(
    scriptSize: Size,
    screenSize: Size,
    scaleRate: Double
): Region {
    val scaledScriptSize = scriptSize * scaleRate

    return Region(
        calculateBorderThickness(
            screenSize.width,
            scaledScriptSize.width
        ), // Offset(X)
        calculateBorderThickness(
            screenSize.height,
            scaledScriptSize.height
        ), // Offset(Y)
        scaledScriptSize.width, // Game Width (without borders)
        scaledScriptSize.height // Game Height (without borders)
    )
}

/**
 * Checks if a region is wider than x:y
 */
fun Size.widerThan(x: Int, y: Int) =
    width / height.toDouble() > x.toDouble() / y

// Looks like only wider than 18:9 uses dynamic scaling, rest stays in 16:9
// Thanks to SeibahMaster from GamePress
fun Size.isWide() =
    widerThan(18, 9)

class FgoGameAreaManager(
    private val gameSizeWithBorders: Size,
    private val offset: () -> Location
) : GameAreaManager {
    companion object {
        private val imageSize = Size(1280, 720)
        private val scriptSize = Size(2560, 1440)
    }

    private val scaleBy = decideScaleMethod(
        scriptSize,
        gameSizeWithBorders
    )

    override val scriptDimension = when (scaleBy) {
        is ScaleBy.Width -> CompareBy.Width(scriptSize.width)
        is ScaleBy.Height -> CompareBy.Height(scriptSize.height)
    }

    override val compareDimension = when (scaleBy) {
        is ScaleBy.Width -> CompareBy.Width(imageSize.width)
        is ScaleBy.Height -> CompareBy.Height(imageSize.height)
    }

    private val isWide = gameSizeWithBorders.isWide()
    private val isUltraWide = gameSizeWithBorders.widerThan(21, 9)

    private val gameAreaIgnoringNotch by lazy {
        when {
            // For wider than 21:9 screens, blue borders appear on sides
            isUltraWide -> calculateGameAreaWithoutBorders(
                Size(3360, 1440),
                gameSizeWithBorders,
                scaleBy.rate
            )
            isWide -> Region(Location(), gameSizeWithBorders)
            else -> calculateGameAreaWithoutBorders(
                scriptSize,
                gameSizeWithBorders,
                scaleBy.rate
            )
        }
    }

    override val gameArea
        get() = gameAreaIgnoringNotch + offset()
}