package com.mathewsachin.fategrandautomata.core

/**
 * Calculates the ratio between the screen resolution and the image resolution.
 *
 * @return the ratio or `null` if the resolution is the same
 */
fun screenToImageScale(): Double? {
    val targetDimensions = GameAreaManager.CompareDimension
        ?: GameAreaManager.ScriptDimension
        ?: return null

    val gameArea = GameAreaManager.GameArea

    if (targetDimensions.CompareByWidth) {
        if (targetDimensions.Pixels == gameArea.Width) {
            return null
        }

        return targetDimensions.Pixels / gameArea.Width.toDouble()
    }

    if (targetDimensions.Pixels == gameArea.Height) {
        return null
    }

    return targetDimensions.Pixels / gameArea.Height.toDouble()
}

const val DontScale = 1.0

/**
 * Calculates the ratio between the script resolution and the screen resolution.
 */
fun scriptToScreenScale(): Double {
    if (GameAreaManager.ScriptDimension == null) {
        return DontScale
    }

    val sourceRegion = GameAreaManager.ScriptDimension
        ?: return DontScale

    val targetRegion = GameAreaManager.GameArea

    val pixels = sourceRegion.Pixels

    if (sourceRegion.CompareByWidth) {
        if (targetRegion.Width == pixels) {
            return DontScale
        }

        return targetRegion.Width / pixels.toDouble()
    }

    if (targetRegion.Height == pixels) {
        return DontScale
    }

    return targetRegion.Height / pixels.toDouble()
}

/**
 * Transforms the current [Location] in script coordinates to a new one in screen coordinates.
 *
 * @return a new [Location] in screen coordinates
 */
fun Location.transform(): Location {
    val scale = scriptToScreenScale()
    val scaledPoint = this * scale
    val gameArea = GameAreaManager.GameArea

    return Location(scaledPoint.X + gameArea.X, scaledPoint.Y + gameArea.Y)
}

/**
 * Transforms the current [Region] in script coordinates to a new one in screen coordinates.
 *
 * @return a new [Region] in screen coordinates
 */
fun Region.transform(): Region {
    val scale = scriptToScreenScale()

    val scaledPoint = location.transform()
    val scaledSize = size * scale

    return Region(scaledPoint.X, scaledPoint.Y, scaledSize.Width, scaledSize.Height)
}

/**
 * Calculates the ratio between the script resolution and the image resolution.
 */
fun scriptToImageScale(): Double {
    // Script -> Screen
    val scale1 = scriptToScreenScale()

    // Screen -> Image
    val scale2 = screenToImageScale() ?: DontScale

    return scale1 * scale2
}

/**
 * Transforms the [Region] from script coordinates to image coordinates.
 *
 * Normally, the script coordinates are in 1440p while the image coordinates are in 720p.
 *
 * @return a new [Region] with the same position in screen coordinates.
 */
fun Region.transformToImage(): Region {
    return this * scriptToImageScale()
}

/**
 * Transforms the [Region] from image coordinates to script coordinates.
 *
 * Normally, the script coordinates are in 1440p while the image coordinates are in 720p.
 *
 * @return a new [Region] with the same position in script coordinates.
 */
fun Region.transformFromImage(): Region {
    return this * (1 / scriptToImageScale())
}