package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
import com.mathewsachin.fategrandautomata.scripts.prefs.isNewUI
import com.mathewsachin.libautomata.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private sealed class ScaleBy(val rate: Double) : Comparable<ScaleBy> {
    class Width(rate: Double) : ScaleBy(rate)
    class Height(rate: Double) : ScaleBy(rate)

    override fun compareTo(other: ScaleBy) =
        rate.compareTo(other.rate)
}

private fun decideScaleMethod(OriginalSize: Size, DesiredSize: Size) =
    minOf(
        ScaleBy.Width(DesiredSize.width / OriginalSize.width.toDouble()),
        ScaleBy.Height(DesiredSize.height / OriginalSize.height.toDouble())
    )

private fun calculateBorderThickness(Outer: Int, Inner: Int) =
    ((Outer - Inner).absoluteValue / 2.0).roundToInt()

private fun calculateGameAreaWithoutBorders(
    ScriptSize: Size,
    ScreenSize: Size,
    ScaleRate: Double
): Region {
    val scaledScriptSize = ScriptSize * ScaleRate

    return Region(
        calculateBorderThickness(
            ScreenSize.width,
            scaledScriptSize.width
        ), // Offset(X)
        calculateBorderThickness(
            ScreenSize.height,
            scaledScriptSize.height
        ), // Offset(Y)
        scaledScriptSize.width, // Game Width (without borders)
        scaledScriptSize.height // Game Height (without borders)
    )
}

// Looks like only wider than 18:9 uses dynamic scaling, rest stays in 16:9
// Thanks to SeibahMaster from GamePress
fun Region.isWide() =
    width / height.toDouble() > 18.0 / 9

class FgoGameAreaManager(
    val platformImpl: IPlatformImpl,
    val prefs: IPreferences
) : GameAreaManager {
    private val imageSize = Size(1280, 720)
    private val scriptSize = Size(2560, 1440)

    private val gameWithBorders = platformImpl.windowRegion
    private val scaleBy = decideScaleMethod(
        scriptSize,
        gameWithBorders.size
    )
    private val gameAreaIgnoringNotch =
        calculateGameAreaWithoutBorders(
            scriptSize,
            gameWithBorders.size,
            scaleBy.rate
        )

    override val scriptDimension = when (scaleBy) {
        is ScaleBy.Width -> CompareBy.Width(scriptSize.width)
        is ScaleBy.Height -> CompareBy.Height(scriptSize.height)
    }

    override val compareDimension = when (scaleBy) {
        is ScaleBy.Width -> CompareBy.Width(imageSize.width)
        is ScaleBy.Height -> CompareBy.Height(imageSize.height)
    }

    val isWide = prefs.isNewUI && platformImpl.windowRegion.isWide()

    override val gameArea
        get() =
            if (isWide) {
                platformImpl.windowRegion
            } else gameAreaIgnoringNotch + platformImpl.windowRegion.location
}