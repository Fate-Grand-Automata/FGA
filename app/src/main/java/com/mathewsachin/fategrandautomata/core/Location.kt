package com.mathewsachin.fategrandautomata.core

import kotlin.math.roundToInt

data class Location(val X: Int = 0, val Y: Int = 0) {
    operator fun times(scale: Double): Location {
        return Location(
            (X * scale).roundToInt(),
            (Y * scale).roundToInt()
        )
    }
}