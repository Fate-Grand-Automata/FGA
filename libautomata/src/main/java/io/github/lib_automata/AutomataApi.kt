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

    fun Map<Pattern, Region>.exists(
        timeout: Duration = Duration.ZERO,
        similarity: Double? = null,
        requireAll: Boolean = false
    ): Boolean
}