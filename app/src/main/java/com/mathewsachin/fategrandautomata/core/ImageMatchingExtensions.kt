package com.mathewsachin.fategrandautomata.core

import com.mathewsachin.fategrandautomata.core.ExitManager.checkExitRequested
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic
import kotlin.time.milliseconds

/**
 * The default minimum similarity used for image comparisons.
 */
private const val MinSimilarity = 0.8

/**
 * Checks if the [Region] contains the provided image.
 *
 * @param Region the search region
 * @param Image the image to look for
 * @param Similarity the minimum similarity for this search
 */
private fun existsNow(
    Region: Region,
    Image: IPattern,
    Similarity: Double = MinSimilarity
): Boolean {
    checkExitRequested()

    var sshot = ScreenshotManager.getScreenshot()

    if (Preferences.DebugMode) {
        Region.highlight()
    }

    sshot = sshot?.crop(Region.transformToImage())

    return sshot?.isMatch(Image, Similarity) ?: false
}

/**
 * Repeats the invocation of the Condition until it returns `true` or until the timeout has
 * been reached.
 *
 * @param Condition a function with a [Boolean] return value
 * @param Timeout how long to wait for before giving up
 * @return `true` if the function returned `true` at some point, `false` if the timeout was
 * reached
 */
private fun checkConditionLoop(
    Condition: () -> Boolean,
    Timeout: Duration = Duration.ZERO
): Boolean {
    val endTimeMark = Monotonic.markNow() + Timeout

    while (true) {
        val scanStart = Monotonic.markNow()

        if (Condition.invoke()) {
            return true
        }

        // check if we need to cancel because of timeout
        if (endTimeMark.hasPassedNow()) {
            break
        }

        /* Wait a bit before checking again.
           If invocationDuration is greater than the scanInterval, we don't wait. */
        val scanInterval = 330.milliseconds
        val timeToWait = scanInterval - scanStart.elapsedNow()

        if (timeToWait.isPositive()) {
            timeToWait.wait()
        }
    }

    return false
}


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
    Similarity: Double = MinSimilarity
): Boolean {
    checkExitRequested()
    return checkConditionLoop(
        { existsNow(this, Image, Similarity) },
        Timeout
    )
}

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
    Similarity: Double = MinSimilarity
): Boolean {
    checkExitRequested()
    return checkConditionLoop(
        { !existsNow(this, Image, Similarity) },
        Timeout
    )
}

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
    Similarity: Double = MinSimilarity
): Sequence<Match> {
    var sshot = ScreenshotManager.getScreenshot()

    if (Preferences.DebugMode) {
        this.highlight()
    }

    sshot = sshot!!.crop(this.transformToImage())

    return sshot
        .findMatches(Pattern, Similarity)
        .map {
            checkExitRequested()

            var region = it.Region.transformFromImage()

            // convert the relative position in the region to the absolute position on the screen
            region += this.location

            Match(region, it.score)
        }
}
