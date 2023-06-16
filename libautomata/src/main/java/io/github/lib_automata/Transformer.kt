package io.github.lib_automata

import javax.inject.Inject

interface Transformer {
    /**
     * Transforms the current [Location] in script coordinates to a new one in screen coordinates.
     *
     * @return a new [Location] in screen coordinates
     */
    fun toScreen(location: Location): Location

    /**
     * Transforms the current [Region] in script coordinates to a new one in screen coordinates.
     *
     * @return a new [Region] in screen coordinates
     */
    fun toScreen(region: Region): Region

    /**
     * Transforms the [Region] from script coordinates to image coordinates.
     *
     * Normally, the script coordinates are in 1440p while the image coordinates are in 720p.
     *
     * @return a new [Region] with the same position in screen coordinates.
     */
    fun toImage(region: Region): Region

    /**
     * Transforms the [Region] from image coordinates to script coordinates.
     *
     * Normally, the script coordinates are in 1440p while the image coordinates are in 720p.
     *
     * @return a new [Region] with the same position in script coordinates.
     */
    fun fromImage(region: Region): Region
}

class RealTransformer @Inject constructor(
    private val gameAreaManager: GameAreaManager,
    private val scale: Scale
) : Transformer {
    override fun toScreen(location: Location): Location {
        val scaledPoint = location * scale.scriptToScreen
        val gameArea = gameAreaManager.gameArea

        return scaledPoint + gameArea.location
    }

    override fun toScreen(region: Region): Region {
        val scaledPoint = toScreen(region.location)
        val scaledSize = region.size * scale.scriptToScreen

        return Region(scaledPoint, scaledSize)
    }

    override fun toImage(region: Region) =
        region * scale.scriptToImage

    override fun fromImage(region: Region) =
        region * (1 / scale.scriptToImage)
}