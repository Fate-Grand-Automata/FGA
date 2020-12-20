package com.mathewsachin.libautomata

import kotlin.math.roundToInt

/**
 * Represents a rectangular region.
 */
data class Region(val X: Int, val Y: Int, val Width: Int, val Height: Int) : Comparable<Region> {
    init {
        require(Width > 0) { "Width must be positive" }
        require(Height > 0) { "Height must be positive" }
    }

    constructor(Location: Location, Size: Size) : this(
        Location.X,
        Location.Y,
        Size.Width,
        Size.Height
    )

    /**
     * Creates a copy of this [Region] where X, Y, Width, and Height have been multiplied with the
     * given number.
     */
    operator fun times(scale: Double): Region {
        return Region(
            (X * scale).roundToInt(),
            (Y * scale).roundToInt(),
            (Width * scale).roundToInt(),
            (Height * scale).roundToInt()
        )
    }

    /**
     * Creates a new [Region] width the same width and height, but X and Y are increased by the
     * [Location]'s X and Y value.
     */
    operator fun plus(Location: Location): Region {
        return Region(location + Location, size)
    }

    /**
     * Creates a new [Region] width the same width and height, but X and Y are decreased by the
     * [Location]'s X and Y value.
     */
    operator fun minus(Location: Location): Region {
        return Region(location - Location, size)
    }

    /**
     * Returns the upper left corner position as [Location].
     */
    val location get() = Location(X, Y)

    /**
     * Returns the size of the region as [Size] object.
     */
    val size get() = Size(Width, Height)

    /**
     * Returns the center point of the region.
     */
    val center get() = Location(X + Width / 2, Y + Height / 2)

    /**
     * Returns the X coordinate of the right border.
     */
    val right get() = X + Width

    /**
     * Returns the Y coordinate of the bottom border.
     */
    val bottom get() = Y + Height

    /**
     * Returns the intersection between this [Region] and another [Region].
     *
     * If there is no intersection, a 1x1 Region is returned, where X and Y are taken from `this`.
     */
    fun clip(Region: Region): Region {
        val left = Region.X.coerceIn(X, right - 1)
        val right = Region.right.coerceIn(X + 1, right)
        val top = Region.Y.coerceIn(Y, bottom - 1)
        val bottom = Region.bottom.coerceIn(Y + 1, bottom)

        return Region(left, top, right - left, bottom - top)
    }

    /**
     * Checks if the given [Region] is fully contained in this [Region].
     */
    operator fun contains(Region: Region): Boolean {
        return X <= Region.X
                && Y <= Region.Y
                && right >= Region.right
                && bottom >= Region.bottom
    }

    override fun compareTo(other: Region) =
        location.compareTo(other.location)
}