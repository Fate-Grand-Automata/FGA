package io.github.lib_automata

import java.io.OutputStream

/**
 * Interface for image objects.
 */
interface Pattern : AutoCloseable {
    val width: Int
    val height: Int
    var tag: String

    /**
     * Creates a resized image with the specified size.
     *
     * @param size the size of the new image
     */
    fun resize(size: Size): Pattern

    /**
     * Creates a resized image with the specified size and writes it into the target.
     *
     * @param target the image to write the resized image data to
     * @param size the size of the new image
     */
    fun resize(target: Pattern, size: Size)

    /**
     * Finds all image matches with high enough similarity values.
     *
     * @param template the image to match with
     * @param similarity the minimum similarity
     *
     * @return a list of [Match] objects
     */
    fun findMatches(template: Pattern, similarity: Double): Sequence<Match>

    /**
     * Crops the image to be within the bounds of the given [region].
     *
     * Note that the resulting [Pattern] can have a smaller size than the [region] if the [region]
     * is not fully contained in the area of the image.
     *
     * @param region a [Region] in image coordinates, see [Transformer.toImage]
     */
    fun crop(region: Region): Pattern

    fun save(stream: OutputStream)

    /**
     * Makes a copy of the image.
     */
    fun copy(): Pattern

    fun threshold(value: Double): Pattern

    fun isWhite(): Boolean

    fun isBlack(): Boolean

    fun floodFill(x: Double, y: Double, maxDiff: Double, newValue: Double): Pattern

    fun fillText(): Pattern

    /**
     * Returns the average brightness of the pattern's image.
     * @return average brightness (0.0 = dark, 255.0 = bright)
     */
    fun getAverageBrightness(): Double

    /**
     * Returns the minimum and maximum brightness of the pattern's image.
     * @return Pair(minBrightness, maxBrightness) (0.0 = dark, 255.0 = bright)
     */
    fun getMinMaxBrightness(): Pair<Double, Double>

    /**
     * Checks if the average Saturation (S) and Value (V) of this Pattern
     * exceed the specified thresholds.
     *
     * ⚠️ When calling this function inside `useSameSnapIn`, be aware that it may
     * reuse a gray cached snapshot and return incorrect results.
     *
     * @param sThresh Saturation threshold.
     * @param vThresh Value (brightness) threshold.
     * @return Boolean True if both average S and V exceed the thresholds.
     */
    fun isSaturationAndValueOver(sThresh: Double, vThresh: Double): Boolean

    /**
     * Computes the average hue, saturation, and value (HSV) of this image region.
     */
    fun getHsvAverage(): Hsv

    /**
     * Normalizes the image by masking pixels within the specified HSV range
     * and converting the result into a binary image.
     *
     * ⚠️ When calling this function inside `useSameSnapIn`, be aware that it may
     * reuse a gray cached snapshot and return incorrect results.
     *
     * @param lower the lower bound of the HSV range
     * @param upper the upper bound of the HSV range
     * @param invert if true, the binary result is inverted.
     * 　　　　　　　　 Set to true for white text
     * @return a [Pattern] containing the normalized image
     */
    fun normalizeByHsv(lower: Hsv, upper: Hsv, invert: Boolean = false): Pattern

    /**
     * Crops the given binary image to the bounding rectangle of non-zero pixels.
     * Returns the cropped image as a new Mat.
     */
    fun cropWhiteRegion(pad: Int = 2): Pattern

    /**
     * Counts the number of columns (HORIZONTAL) or rows (VERTICAL)
     * that contain pixels within the specified HSV range.
     *
     * @param lower HSV lower bound for the target color.
     * @param upper HSV upper bound for the target color.
     * @param axis Direction to count: HORIZONTAL (columns) or VERTICAL (rows)
     * @return The number of columns or rows containing pixels within the HSV range.
     */
    fun countPixelsInHsvRange(lower: Hsv, upper: Hsv, axis: Axis = Axis.HORIZONTAL): Int
}

enum class Axis { HORIZONTAL, VERTICAL }

/**
 * Gets the width and height in the form of a [Size] object.
 */
val Pattern.size get() = Size(width, height)