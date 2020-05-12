package com.mathewsachin.fategrandautomata.core

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

fun Location.transform(): Location {
    val scale = scriptToScreenScale()
    val scaledPoint = this * scale
    val gameArea = GameAreaManager.GameArea

    return Location(scaledPoint.X + gameArea.X, scaledPoint.Y + gameArea.Y)
}

fun Region.transform(): Region {
    val scale = scriptToScreenScale()

    val scaledPoint = location.transform()
    val scaledSize = size * scale

    return Region(scaledPoint.X, scaledPoint.Y, scaledSize.Width, scaledSize.Height)
}

fun scriptToImageScale(): Double {
    // Script -> Screen
    val scale1 = scriptToScreenScale()

    // Screen -> Image
    val scale2 = screenToImageScale() ?: DontScale

    return scale1 * scale2
}

fun Region.transformToImage(): Region {
    return this * scriptToImageScale()
}

fun Region.transformFromImage(): Region {
    return this * (1 / scriptToImageScale())
}