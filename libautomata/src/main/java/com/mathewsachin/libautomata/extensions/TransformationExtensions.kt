package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.CompareBy
import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import javax.inject.Inject

class TransformationExtensions @Inject constructor(
    val gameAreaManager: GameAreaManager
) : ITransformationExtensions {
    val noScaling = 1.0

    override fun screenToImageScale(): Double? {
        val targetDimensions =
            if (gameAreaManager.compareDimension !is CompareBy.None) {
                gameAreaManager.compareDimension
            } else gameAreaManager.scriptDimension

        val gameArea = gameAreaManager.gameArea

        return when (targetDimensions) {
            is CompareBy.Width -> {
                if (targetDimensions.width == gameArea.width) {
                    null
                } else targetDimensions.width / gameArea.width.toDouble()
            }
            is CompareBy.Height -> {
                if (targetDimensions.height == gameArea.height) {
                    null
                } else targetDimensions.height / gameArea.height.toDouble()
            }
            CompareBy.None -> null
        }
    }

    override fun scriptToScreenScale(): Double {
        val sourceRegion = gameAreaManager.scriptDimension

        val targetRegion = gameAreaManager.gameArea

        return when (sourceRegion) {
            is CompareBy.Width -> {
                if (targetRegion.width == sourceRegion.width) {
                    noScaling
                } else targetRegion.width / sourceRegion.width.toDouble()
            }
            is CompareBy.Height -> {
                if (targetRegion.height == sourceRegion.height) {
                    noScaling
                } else targetRegion.height / sourceRegion.height.toDouble()
            }
            CompareBy.None -> noScaling
        }
    }

    override fun Location.transform(): Location {
        val scale = scriptToScreenScale()
        val scaledPoint = this * scale
        val gameArea = gameAreaManager.gameArea

        return scaledPoint + gameArea.location
    }

    override fun Region.transform(): Region {
        val scale = scriptToScreenScale()

        val scaledPoint = location.transform()
        val scaledSize = size * scale

        return Region(scaledPoint, scaledSize)
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