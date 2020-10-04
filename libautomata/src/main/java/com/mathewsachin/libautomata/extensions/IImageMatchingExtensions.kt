package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Match
import com.mathewsachin.libautomata.Region
import kotlin.time.Duration

interface IImageMatchingExtensions {
    /**
     * Checks if the [Region] contains the provided image.
     *
     * @param Image the image to look for
     * @param Timeout how long to search for before giving up
     * @param Similarity the minimum similarity for this search
     */
    fun Region.exists(
        Image: IPattern,
        Timeout: Duration = Duration.ZERO,
        Similarity: Double? = null
    ): Boolean

    operator fun Region.contains(image: IPattern) = exists(image)

    /**
     * Waits until the given image cannot be found in the [Region] anymore.
     *
     * @param Image the image to search for
     * @param Timeout how long to wait for before giving up
     * @param Similarity the minimum similarity for this search
     */
    fun Region.waitVanish(
        Image: IPattern,
        Timeout: Duration,
        Similarity: Double? = null
    ): Boolean

    /**
     * Searches for all occurrences of a given image in the [Region].
     *
     * @param Pattern the image to search for
     * @param Similarity the minimum similarity for this search
     *
     * @return a list of all matches in the form of [Match] objects
     */
    fun Region.findAll(
        Pattern: IPattern,
        Similarity: Double? = null
    ): Sequence<Match>

    fun Region.find(
        Pattern: IPattern,
        Similarity: Double? = null
    ): Match? = findAll(Pattern, Similarity).firstOrNull()
}