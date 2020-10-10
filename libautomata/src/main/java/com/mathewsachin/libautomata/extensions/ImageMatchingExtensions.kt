package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic
import kotlin.time.milliseconds

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
     * @param Region the search region
     * @param Image the image to look for
     * @param Similarity the minimum similarity for this search
     */
    private fun existsNow(
        Region: Region,
        Image: IPattern,
        Similarity: Double?
    ) = Region.findAll(Image, Similarity).any()

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

    override fun Region.exists(
        Image: IPattern,
        Timeout: Duration,
        Similarity: Double?
    ): Boolean {
        exitManager.checkExitRequested()
        return checkConditionLoop(
            { existsNow(this, Image, Similarity) },
            Timeout
        )
    }

    override fun Region.waitVanish(
        Image: IPattern,
        Timeout: Duration,
        Similarity: Double?
    ): Boolean {
        exitManager.checkExitRequested()
        return checkConditionLoop(
            { !existsNow(this, Image, Similarity) },
            Timeout
        )
    }

    override fun Region.findAll(
        Pattern: IPattern,
        Similarity: Double?
    ): Sequence<Match> {
        val similarity = Similarity ?: platformImpl.prefs.minSimilarity

        return screenshotManager.getScreenshot()
            .crop(this.transformToImage())
            .findMatches(Pattern, similarity)
            .map {
                exitManager.checkExitRequested()

                var region = it.Region.transformFromImage()

                // convert the relative position in the region to the absolute position on the screen
                region += this.location

                Match(region, it.score)
            }
            .also {
                if (platformImpl.prefs.debugMode) {
                    this.highlight(success = it.any())
                }
            }
    }
}
