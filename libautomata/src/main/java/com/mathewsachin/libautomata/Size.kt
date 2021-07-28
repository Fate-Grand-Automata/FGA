package com.mathewsachin.libautomata

import kotlin.math.roundToInt

/**
 * A class for storing the [width] and [height] of something.
 */
data class Size(val width: Int, val height: Int) {
    init {
        require(width > 0) { "width must be positive" }
        require(height > 0) { "height must be positive" }
    }

    /**
     * Returns a new [Size] where X and Y were multiplied with [scale].
     */
    operator fun times(scale: Double): Size {
        return Size(
            (width * scale).roundToInt(),
            (height * scale).roundToInt()
        )
    }
}