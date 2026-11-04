package io.github.fate_grand_automata.atlas

import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Turns a servant's face art into the image FGA matches against the support list.
 *
 * The game draws the 128x128 face art of a servant at 157x157 and shows a 125x44 window of it,
 * 3px from the left and 47px from the top. FGA matches at 720p, so cutting that window out of the
 * art at that size reproduces the pixels the support list shows — no in-game screenshot needed.
 * The sizes were measured by matching the images shipped in `assets/Support/servant` against the
 * face art they were made from; the same art is served for every region.
 */
object SupportServantImage {
    /** The size of a support image, in FGA's 720p matching resolution. */
    const val WIDTH = 125
    const val HEIGHT = 44

    private const val RENDER_SIZE = 157
    private const val OFFSET_X = 3
    private const val OFFSET_Y = 47

    /**
     * [pixels] holds the face art row by row as ARGB, [width] x [height] of them.
     *
     * Returns [WIDTH] x [HEIGHT] ARGB pixels, without the part above, below or left of the
     * window the game shows.
     */
    fun render(pixels: IntArray, width: Int, height: Int): IntArray {
        require(width > 0 && height > 0) { "Empty face image" }
        require(pixels.size >= width * height) {
            "Expected ${width * height} pixels, got ${pixels.size}"
        }

        val rendered = scale(pixels, width, height, RENDER_SIZE, RENDER_SIZE)

        return IntArray(WIDTH * HEIGHT).also { window ->
            for (y in 0 until HEIGHT) {
                val start = (OFFSET_Y + y) * RENDER_SIZE + OFFSET_X
                rendered.copyInto(window, y * WIDTH, start, start + WIDTH)
            }
        }
    }

    /** Bilinear scaling, the same family of filter the game's own scaling uses. */
    private fun scale(
        pixels: IntArray,
        width: Int,
        height: Int,
        newWidth: Int,
        newHeight: Int
    ): IntArray {
        val scaled = IntArray(newWidth * newHeight)
        val xRatio = width.toDouble() / newWidth
        val yRatio = height.toDouble() / newHeight

        for (y in 0 until newHeight) {
            val sourceY = (y + 0.5) * yRatio - 0.5
            val y0 = floor(sourceY).toInt().coerceIn(0, height - 1)
            val y1 = (y0 + 1).coerceAtMost(height - 1)
            val yWeight = (sourceY - y0).coerceIn(0.0, 1.0)

            for (x in 0 until newWidth) {
                val sourceX = (x + 0.5) * xRatio - 0.5
                val x0 = floor(sourceX).toInt().coerceIn(0, width - 1)
                val x1 = (x0 + 1).coerceAtMost(width - 1)
                val xWeight = (sourceX - x0).coerceIn(0.0, 1.0)

                scaled[y * newWidth + x] = blend(
                    pixels[y0 * width + x0], pixels[y0 * width + x1],
                    pixels[y1 * width + x0], pixels[y1 * width + x1],
                    xWeight, yWeight
                )
            }
        }

        return scaled
    }

    private fun blend(
        topLeft: Int,
        topRight: Int,
        bottomLeft: Int,
        bottomRight: Int,
        xWeight: Double,
        yWeight: Double
    ): Int = CHANNEL_SHIFTS.fold(0) { blended, shift ->
        val top = channel(topLeft, shift) * (1 - xWeight) + channel(topRight, shift) * xWeight
        val bottom =
            channel(bottomLeft, shift) * (1 - xWeight) + channel(bottomRight, shift) * xWeight

        val value = (top * (1 - yWeight) + bottom * yWeight).roundToInt().coerceIn(0, 255)

        blended or (value shl shift)
    }

    /** ARGB, most significant channel first. */
    private val CHANNEL_SHIFTS = intArrayOf(24, 16, 8, 0)

    private fun channel(pixel: Int, shift: Int) = ((pixel shr shift) and 0xFF).toDouble()
}
