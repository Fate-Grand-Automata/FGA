package com.mathewsachin.libautomata

import kotlin.math.roundToInt

/**
 * A class for storing the width and height of something.
 */
data class Size(val Width: Int, val Height: Int) {
    init {
        require(Width > 0) { "Width must be positive" }
        require(Height > 0) { "Height must be positive" }
    }

    /**
     * Returns a new [Size] where X and Y were multiplied with [scale].
     */
    operator fun times(scale: Double): Size {
        return Size(
            (Width * scale).roundToInt(),
            (Height * scale).roundToInt()
        )
    }
}