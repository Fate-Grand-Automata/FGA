package com.mathewsachin.fategrandautomata.core

import kotlin.math.roundToInt

/**
 * Represents a 2D coordinate.
 */
data class Location(val X: Int = 0, val Y: Int = 0) {
    /**
     * Creates a copy of this [Location] where X and Y have been multiplied with the given value.
     */
    operator fun times(scale: Double): Location {
        return Location(
            (X * scale).roundToInt(),
            (Y * scale).roundToInt()
        )
    }

    /**
     * Creates a new [Location] where `X == this.X + Other.X` and `Y == this.Y + Other.Y`.
     */
    operator fun plus(Other: Location): Location {
        return Location(X + Other.X, Y + Other.Y)
    }

    /**
     * Creates a new [Location] where `X == this.X - Other.X` and `Y == this.Y - Other.Y`.
     */
    operator fun minus(Other: Location): Location {
        return Location(X - Other.X, Y - Other.Y)
    }
}