package io.github.lib_automata

import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

interface ImageMatcher {
    /**
     * Checks if the [Region] contains the provided image.
     *
     * @param image the image to look for
     * @param timeout how long to search for before giving up
     * @param similarity the minimum similarity for this search
     */
    fun exists(
        region: Region,
        image: Pattern,
        timeout: Duration = Duration.ZERO,
        similarity: Double? = null
    ): Boolean

    /**
     * Waits until the given image cannot be found in the [Region] anymore.
     *
     * @param image the image to search for
     * @param timeout how long to wait for before giving up
     * @param similarity the minimum similarity for this search
     */
    fun waitVanish(
        region: Region,
        image: Pattern,
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
    fun findAll(
        region: Region,
        pattern: Pattern,
        similarity: Double? = null
    ): Sequence<Match>

    fun isWhite(region: Region): Boolean
    fun isBlack(region: Region): Boolean

    /**
     * Checks if all images in the list exist in their respective regions.
     *
     * @param items a list of [Region] and [Pattern] pairs
     * @param timeout how long to search for before giving up
     * @param similarity the minimum similarity for this search
     * @param requireAll if `true`, all images must exist in their respective regions
     */
    fun existsInList(
        items: List<Pair<Region, Pattern>>,
        timeout: Duration = Duration.ZERO,
        similarity: Double? = null,
        requireAll: Boolean
    ): Boolean
}

class RealImageMatcher @Inject constructor(
    private val exitManager: ExitManager,
    private val screenshotManager: ScreenshotManager,
    private val platformImpl: PlatformImpl,
    private val wait: Waiter,
    private val highlight: Highlighter,
    private val transform: Transformer
) : ImageMatcher {
    /**
     * Checks if the [Region] contains the provided image.
     *
     * @param image the image to look for
     * @param similarity the minimum similarity for this search
     */
    private fun Region.existsNow(
        image: Pattern,
        similarity: Double?
    ) = findAll(this, image, similarity).any()

    /**
     * Repeats the invocation of the Condition until it returns `true` or until the timeout has
     * been reached.
     *
     * @param condition a function with a [Boolean] return value
     * @param timeout how long to wait for before giving up
     * @return `true` if the function returned `true` at some point, `false` if the timeout was
     * reached
     */
    private fun checkConditionLoop(
        condition: () -> Boolean,
        timeout: Duration = Duration.ZERO
    ): Boolean {
        val endTimeMark = TimeSource.Monotonic.markNow() + timeout

        while (true) {
            val scanStart = TimeSource.Monotonic.markNow()

            if (condition.invoke()) {
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
                wait(timeToWait)
            }
        }

        return false
    }

    override fun exists(region: Region, image: Pattern, timeout: Duration, similarity: Double?): Boolean {
        exitManager.checkExitRequested()
        return checkConditionLoop(
            { region.existsNow(image, similarity) },
            timeout
        )
    }

    override fun waitVanish(region: Region, image: Pattern, timeout: Duration, similarity: Double?): Boolean {
        exitManager.checkExitRequested()
        return checkConditionLoop(
            { !region.existsNow(image, similarity) },
            timeout
        )
    }

    override fun findAll(region: Region, pattern: Pattern, similarity: Double?) =
        screenshotManager.getScreenshot()
            .crop(transform.toImage(region))
            .findMatches(
                pattern,
                similarity ?: platformImpl.prefs.minSimilarity
            )
            .map {
                exitManager.checkExitRequested()

                // convert the relative position in the region to the absolute position on the screen
                val matchedRegion = transform.fromImage(it.region) + region.location

                Match(matchedRegion, it.score)
            }
            .also {
                highlight(
                    region,
                    color = if (it.any()) HighlightColor.Success else HighlightColor.Error
                )
            }

    override fun isWhite(region: Region) =
        screenshotManager.getScreenshot()
            .crop(transform.toImage(region))
            .isWhite()
            .also {
                highlight(
                    region,
                    color = if (it) HighlightColor.Success else HighlightColor.Error
                )
            }

    override fun isBlack(region: Region) =
        screenshotManager.getScreenshot()
            .crop(transform.toImage(region))
            .isBlack()
            .also {
                highlight(
                    region,
                    color = if (it) HighlightColor.Success else HighlightColor.Error
                )
            }

    override fun existsInList(
        items: List<Pair<Region, Pattern>>,
        timeout: Duration,
        similarity: Double?,
        requireAll: Boolean
    ): Boolean {
        exitManager.checkExitRequested()
        return checkConditionLoop(
            {
                if (requireAll){
                    items.all { (region, image) ->
                        region.existsNow(image, similarity)
                    }
                } else{
                    items.any { (region, image) ->
                        region.existsNow(image, similarity)
                    }
                }
            },
            timeout
        )
    }
}