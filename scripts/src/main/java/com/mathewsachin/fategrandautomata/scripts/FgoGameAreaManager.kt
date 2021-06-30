package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.IPreferences
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
        ScaleBy.Width(DesiredSize.Width / OriginalSize.Width.toDouble()),
        ScaleBy.Height(DesiredSize.Height / OriginalSize.Height.toDouble())
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

// Looks like only wider than 18:9 uses dynamic scaling, rest stays in 16:9
// Thanks to SeibahMaster from GamePress
fun Region.isWide() =
    Width / Height.toDouble() > 18.0 / 9

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
        is ScaleBy.Width -> CompareBy.Width(scriptSize.Width)
        is ScaleBy.Height -> CompareBy.Height(scriptSize.Height)
    }

    override val compareDimension = when (scaleBy) {
        is ScaleBy.Width -> CompareBy.Width(imageSize.Width)
        is ScaleBy.Height -> CompareBy.Height(imageSize.Height)
    }

    val isWide = prefs.gameServer == GameServerEnum.Jp
            && platformImpl.windowRegion.isWide()

    override val gameArea
        get() =
            if (isWide) {
                platformImpl.windowRegion
            } else gameAreaIgnoringNotch + platformImpl.windowRegion.location
}