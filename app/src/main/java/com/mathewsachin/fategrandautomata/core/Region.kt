package com.mathewsachin.fategrandautomata.core

import kotlin.math.roundToInt

data class Region(val X: Int, val Y: Int, val Width: Int, val Height: Int) {
    init {
        require(Width > 0) { "Width must be positive" }
        require(Height > 0) { "Height must be positive" }
    }

    constructor(Location: Location, Size: Size) : this(Location.X, Location.Y, Size.Width, Size.Height)

    operator fun times(scale: Double): Region {
        return Region(
            (X * scale).roundToInt(),
            (Y * scale).roundToInt(),
            (Width * scale).roundToInt(),
            (Height * scale).roundToInt()
        )
    }

    val location get() = Location(X, Y)

    val size get() = Size(Width, Height)

    val center get() = Location(X + Width / 2, Y + Height / 2)

    val right get() = X + Width

    val bottom get() = Y + Height

    fun clip(Region: Region): Region {
        val left = Region.X.coerceIn(X, right - 1)
        val right = Region.right.coerceIn(X + 1, right)
        val top = Region.Y.coerceIn(Y, bottom - 1)
        val bottom = Region.bottom.coerceIn(Y + 1, bottom)

        return Region(left, top, right - left, bottom - top)
    }

    fun contains(Region: Region): Boolean {
        return X <= Region.X
                && Y <= Region.Y
                && right >= Region.right
                && bottom >= Region.bottom
    }
}