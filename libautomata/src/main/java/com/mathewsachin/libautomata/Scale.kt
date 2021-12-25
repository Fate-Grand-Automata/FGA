package com.mathewsachin.libautomata

import javax.inject.Inject

interface Scale {
    companion object {
        const val NoScaling = 1.0
    }

    /**
     * Calculates the ratio between the screen resolution and the image resolution.
     *
     * @return the ratio or `null` if the resolution is the same
     */
    val screenToImage: Double?

    /**
     * Calculates the ratio between the script resolution and the screen resolution.
     */
    val scriptToScreen: Double

    /**
     * Calculates the ratio between the script resolution and the image resolution.
     */
    val scriptToImage: Double
}

class RealScale @Inject constructor(
    private val gameAreaManager: GameAreaManager
) : Scale {
    override val screenToImage: Double?
        get() {
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

    override val scriptToScreen: Double
        get() {
            val sourceRegion = gameAreaManager.scriptDimension

            val targetRegion = gameAreaManager.gameArea

            return when (sourceRegion) {
                is CompareBy.Width -> {
                    if (targetRegion.width == sourceRegion.width) {
                        Scale.NoScaling
                    } else targetRegion.width / sourceRegion.width.toDouble()
                }
                is CompareBy.Height -> {
                    if (targetRegion.height == sourceRegion.height) {
                        Scale.NoScaling
                    } else targetRegion.height / sourceRegion.height.toDouble()
                }
                CompareBy.None -> Scale.NoScaling
            }
        }

    override val scriptToImage: Double
        get() = scriptToScreen * (screenToImage ?: Scale.NoScaling)
}
