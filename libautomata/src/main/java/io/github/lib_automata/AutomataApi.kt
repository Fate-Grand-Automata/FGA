package io.github.lib_automata

import kotlin.time.Duration

interface AutomataApi {
    /**
     * Gets the image content of this Region.
     *
     * @return an [Pattern] object with the image data
     */
    fun Region.getPattern(tag: String = ""): Pattern

    fun <T> useSameSnapIn(block: () -> T): T

    fun <T> useColor(block: () -> T): T

    /**
     * Waits for the specified duration.
     *
     * @param applyMultiplier whether to apply the wait multiplier or not
     */
    fun Duration.wait(applyMultiplier: Boolean = true)

    fun Location.click(times: Int = 1)

    fun Region.click(times: Int = 1) = center.click(times)

    operator fun Region.contains(image: Pattern) = exists(image)

    operator fun Region.contains(images: Collection<Pattern>) = images.any { contains(it) }

    fun Region.find(
        pattern: Pattern,
        similarity: Double? = null
    ): Match? = findAll(pattern, similarity).firstOrNull()

    fun Region.find(
        patterns: Collection<Pattern>,
        similarity: Double? = null
    ): Match? = patterns.firstNotNullOfOrNull { find(it, similarity) }

    fun Region.exists(
        image: Pattern,
        timeout: Duration = Duration.ZERO,
        similarity: Double? = null
    ): Boolean

    fun Region.waitVanish(
        image: Pattern,
        timeout: Duration,
        similarity: Double? = null
    ): Boolean

    fun Region.findAll(
        pattern: Pattern,
        similarity: Double? = null
    ): Sequence<Match>

    fun Region.isWhite(): Boolean

    fun Region.isBlack(): Boolean

    fun Region.detectText(outlinedText: Boolean = false): String

    /**
     * Extracts the numeric value inside the first pair of parentheses
     * from text pixels within the specified HSV range in this Region.
     *
     * ⚠️ When calling this function inside `useSameSnapIn`, be aware that it may
     * reuse a gray cached snapshot and return incorrect results.
     *
     * @param lower the lower bound of the HSV range
     * @param upper the upper bound of the HSV range
     * @param invert if true, the mask is inverted before OCR
     * @return the numeric string inside the first matched parentheses, or an empty string if none found
     */
    fun Region.detectNumberInBrackets(lower: Hsv, upper: Hsv, invert: Boolean = false): String

    fun Map<Pattern, Region>.exists(
        timeout: Duration = Duration.ZERO,
        similarity: Double? = null,
        requireAll: Boolean = false
    ): Boolean

    /**
     * Returns `true` if the average brightness of this [Region] is above [threshold].
     *
     * If the region is in color, it is converted to grayscale before computing
     * the average brightness (0–255 scale).
     *
     * @param threshold Brightness threshold on the 0–255 scale.
     * @return `true` when average brightness >= [threshold], otherwise `false`.
     */
    fun Region.isBrightnessAbove(threshold: Double): Boolean

    /**
     * Checks if the average Saturation (S) and Value (V) of this Region
     * exceed the specified thresholds.
     *
     * ⚠️ When calling this function inside `useSameSnapIn`, be aware that it may
     * reuse a gray cached snapshot and return incorrect results.
     *
     * @param sThresh Saturation threshold.
     * @param vThresh Value (brightness) threshold.
     * @return Boolean True if both average S and V exceed the thresholds.
     */
    fun Region.isSaturationAndValueOver(sThresh: Double, vThresh: Double): Boolean

    /**
     * Detects the horizontal width of pixels within the specified HSV range in this region.
     *
     * @param lower HSV lower bound for the target color.
     * @param upper HSV upper bound for the target color.
     * @return The width of pixels within the HSV range, in pixels.
     */
    fun Region.detectVisualBarLength(lower: Hsv, upper: Hsv): Int

    /**
     * Returns true if the average brightness of this region is below the given threshold.
     *
     * @param threshold The brightness value to compare against.
     */
    fun Region.isBelowBrightness(threshold: Double): Boolean
}