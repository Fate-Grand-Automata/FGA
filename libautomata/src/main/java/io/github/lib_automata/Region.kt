package io.github.lib_automata

import kotlin.math.roundToInt

/**
 * Represents a rectangular region.
 */
data class Region(val x: Int, val y: Int, val width: Int, val height: Int) : Comparable<Region> {
    init {
        require(width > 0) { "width must be positive" }
        require(height > 0) { "height must be positive" }
    }

    constructor(location: Location, size: Size) : this(
        location.x,
        location.y,
        size.width,
        size.height
    )

    /**
     * Creates a copy of this [Region] where [x], [y], [width], and [height] have been multiplied with the
     * given number.
     */
    operator fun times(scale: Double): Region {
        return Region(
            (x * scale).roundToInt(),
            (y * scale).roundToInt(),
            (width * scale).roundToInt(),
            (height * scale).roundToInt()
        )
    }

    /**
     * Creates a new [Region] with the same [width] and [height], but [x] and [y] are increased by the
     * [location]'s x and y value.
     */
    operator fun plus(location: Location): Region {
        return Region(this.location + location, size)
    }

    /**
     * Creates a new [Region] with the same [width] and [height], but [x] and [y] are decreased by the
     * [location]'s x and y value.
     */
    operator fun minus(location: Location): Region {
        return Region(this.location - location, size)
    }

    /**
     * Returns the upper left corner position as [Location].
     */
    val location get() = Location(x, y)

    /**
     * Returns the size of the region as [Size] object.
     */
    val size get() = Size(width, height)

    /**
     * Returns the center point of the region.
     */
    val center get() = Location(x + width / 2, y + height / 2)

    /**
     * Returns the X coordinate of the right border.
     */
    val right get() = x + width

    /**
     * Returns the Y coordinate of the bottom border.
     */
    val bottom get() = y + height

    /**
     * Returns the intersection between this [Region] and another [region].
     *
     * If there is no intersection, a 1x1 Region is returned, where X and Y are taken from `this`.
     */
    fun clip(region: Region): Region {
        val left = region.x.coerceIn(x, right - 1)
        val right = region.right.coerceIn(x + 1, right)
        val top = region.y.coerceIn(y, bottom - 1)
        val bottom = region.bottom.coerceIn(y + 1, bottom)

        return Region(left, top, right - left, bottom - top)
    }

    /**
     * Checks if the given [region] is fully contained in this [Region].
     */
    operator fun contains(region: Region): Boolean {
        return x <= region.x
                && y <= region.y
                && right >= region.right
                && bottom >= region.bottom
    }

    override fun compareTo(other: Region) =
        location.compareTo(other.location)
}