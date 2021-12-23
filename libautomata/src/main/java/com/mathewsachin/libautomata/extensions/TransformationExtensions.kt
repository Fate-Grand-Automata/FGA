package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.GameAreaManager
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.Scale
import javax.inject.Inject

class TransformationExtensions @Inject constructor(
    private val gameAreaManager: GameAreaManager,
    private val scale: Scale
) : ITransformationExtensions {
    override fun Location.transform(): Location {
        val scaledPoint = this * scale.scriptToScreen
        val gameArea = gameAreaManager.gameArea

        return scaledPoint + gameArea.location
    }

    override fun Region.transform(): Region {

        val scaledPoint = location.transform()
        val scaledSize = size * scale.scriptToScreen

        return Region(scaledPoint, scaledSize)
    }

    override fun Region.transformToImage(): Region {
        return this * scale.scriptToImage
    }

    override fun Region.transformFromImage(): Region {
        return this * (1 / scale.scriptToImage)
    }
}