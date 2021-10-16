package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic

class ImageMatchingExtensions @Inject constructor(
    val exitManager: ExitManager,
    val screenshotManager: ScreenshotManager,
    val platformImpl: IPlatformImpl,
    durationExtensions: IDurationExtensions,
    highlightExtensions: IHighlightExtensions,
    transformationExtensions: ITransformationExtensions
) : IImageMatchingExtensions, IDurationExtensions by durationExtensions,
    IHighlightExtensions by highlightExtensions,
    ITransformationExtensions by transformationExtensions {
    /**
     * Checks if the [Region] contains the provided image.
     *
     * @param image the image to look for
     * @param similarity the minimum similarity for this search
     */
    private fun Region.existsNow(
        image: IPattern,
        similarity: Double?
    ) = findAll(image, similarity).any()

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
        val endTimeMark = Monotonic.markNow() + timeout

        while (true) {
            val scanStart = Monotonic.markNow()

            if (condition.invoke()) {
                return true
            }

            // check if we need to cancel because of timeout
            if (endTimeMark.hasPassedNow()) {
                break
            }

            /* Wait a bit before checking again.
               If invocationDuration is greater than the scanInterval, we don't wait. */
            val scanInterval = Duration.milliseconds(330)
            val timeToWait = scanInterval - scanStart.elapsedNow()

            if (timeToWait.isPositive()) {
                timeToWait.wait()
            }
        }

        return false
    }

    override fun Region.exists(
        image: IPattern,
        timeout: Duration,
        similarity: Double?
    ): Boolean {
        exitManager.checkExitRequested()
        return checkConditionLoop(
            { existsNow(image, similarity) },
            timeout
        )
    }

    override fun Region.waitVanish(
        image: IPattern,
        timeout: Duration,
        similarity: Double?
    ): Boolean {
        exitManager.checkExitRequested()
        return checkConditionLoop(
            { !existsNow(image, similarity) },
            timeout
        )
    }

    override fun Region.findAll(
        pattern: IPattern,
        similarity: Double?
    ): Sequence<Match> {
        return screenshotManager.getScreenshot()
            .crop(this.transformToImage())
            .findMatches(
                pattern,
                similarity ?: platformImpl.prefs.minSimilarity
            )
            .map {
                exitManager.checkExitRequested()

                var region = it.region.transformFromImage()

                // convert the relative position in the region to the absolute position on the screen
                region += this.location

                Match(region, it.score)
            }
            .also {
                highlight(color = if (it.any()) HighlightColor.Success else HighlightColor.Error)
            }
    }
}
