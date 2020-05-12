package com.mathewsachin.fategrandautomata.core

import kotlin.math.roundToInt

data class Size(val Width: Int, val Height: Int) {
    init {
        require(Width > 0) { "Width must be positive" }
        require(Height > 0) { "Height must be positive" }
    }

    operator fun times(scale: Double): Size {
        return Size(
            (Width * scale).roundToInt(),
            (Height * scale).roundToInt()
        )
    }
}