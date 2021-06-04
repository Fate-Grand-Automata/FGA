package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Match
import com.mathewsachin.libautomata.Region
import kotlin.time.Duration

interface IImageMatchingExtensions {
    /**
     * Checks if the [Region] contains the provided image.
     *
     * @param image the image to look for
     * @param timeout how long to search for before giving up
     * @param similarity the minimum similarity for this search
     */
    fun Region.exists(
        image: IPattern,
        timeout: Duration = Duration.ZERO,
        similarity: Double? = null
    ): Boolean

    operator fun Region.contains(image: IPattern) = exists(image)

    /**
     * Waits until the given image cannot be found in the [Region] anymore.
     *
     * @param image the image to search for
     * @param timeout how long to wait for before giving up
     * @param similarity the minimum similarity for this search
     */
    fun Region.waitVanish(
        image: IPattern,
        timeout: Duration,
        similarity: Double? = null
    ): Boolean

    /**
     * Searches for all occurrences of a given image in the [Region].
     *
     * @param pattern the image to search for
     * @param similarity the minimum similarity for this search
     *
     * @return a list of all matches in the form of [Match] objects
     */
    fun Region.findAll(
        pattern: IPattern,
        similarity: Double? = null
    ): Sequence<Match>

    fun Region.find(
        pattern: IPattern,
        similarity: Double? = null
    ): Match? = findAll(pattern, similarity).firstOrNull()
}