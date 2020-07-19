package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

class TransformationExtensions(
    val gameAreaManager: GameAreaManager
) : ITransformationExtensions {
    val noScaling = 1.0

    override fun screenToImageScale(): Double? {
        val targetDimensions = gameAreaManager.compareDimension
            ?: gameAreaManager.scriptDimension
            ?: return null

        val gameArea = gameAreaManager.gameArea

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

    override fun scriptToScreenScale(): Double {
        if (gameAreaManager.scriptDimension == null) {
            return noScaling
        }

        val sourceRegion = gameAreaManager.scriptDimension
            ?: return noScaling

        val targetRegion = gameAreaManager.gameArea

        val pixels = sourceRegion.Pixels

        if (sourceRegion.CompareByWidth) {
            if (targetRegion.Width == pixels) {
                return noScaling
            }

            return targetRegion.Width / pixels.toDouble()
        }

        if (targetRegion.Height == pixels) {
            return noScaling
        }

        return targetRegion.Height / pixels.toDouble()
    }

    override fun Location.transform(): Location {
        val scale = scriptToScreenScale()
        val scaledPoint = this * scale
        val gameArea = gameAreaManager.gameArea

        return Location(
            scaledPoint.X + gameArea.X,
            scaledPoint.Y + gameArea.Y
        )
    }

    override fun Region.transform(): Region {
        val scale = scriptToScreenScale()

        val scaledPoint = location.transform()
        val scaledSize = size * scale

        return Region(
            scaledPoint.X,
            scaledPoint.Y,
            scaledSize.Width,
            scaledSize.Height
        )
    }

    override fun scriptToImageScale(): Double {
        // Script -> Screen
        val scale1 = scriptToScreenScale()

        // Screen -> Image
        val scale2 = screenToImageScale() ?: noScaling

        return scale1 * scale2
    }

    override fun Region.transformToImage(): Region {
        return this * scriptToImageScale()
    }

    override fun Region.transformFromImage(): Region {
        return this * (1 / scriptToImageScale())
    }
}