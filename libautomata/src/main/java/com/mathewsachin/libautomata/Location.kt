package com.mathewsachin.libautomata

import kotlin.math.roundToInt

/**
 * Represents a 2D coordinate.
 */
data class Location(val x: Int = 0, val y: Int = 0) : Comparable<Location> {
    /**
     * Creates a copy of this [Location] where [x] and [y] have been multiplied with the given value.
     */
    operator fun times(scale: Double): Location {
        return Location(
            (x * scale).roundToInt(),
            (y * scale).roundToInt()
        )
    }

    /**
     * Creates a new [Location] where `[x] == this.x + [other].x` and `[y] == this.y + [other].y`.
     */
    operator fun plus(other: Location): Location {
        return Location(x + other.x, y + other.y)
    }

    /**
     * Creates a new [Location] where `[x] == this.x - [other].x` and `[y] == this.y - [other].y`.
     */
    operator fun minus(other: Location): Location {
        return Location(x - other.x, y - other.y)
    }

    override fun compareTo(other: Location) = when {
        y > other.y -> 1
        y < other.y -> -1
        x > other.x -> 1
        x < other.x -> -1
        else -> 0
    }
}