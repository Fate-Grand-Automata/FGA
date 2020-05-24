package com.mathewsachin.fategrandautomata.core

import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import java.io.InputStream
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.TimeSource.Monotonic
import kotlin.time.milliseconds
import kotlin.time.seconds

/**
 * Checks if the stop button has been pressed.
 *
 * @throws ScriptAbortException if the button has been pressed
 */
fun checkExitRequested() {
    if (AutomataApi.exitRequested) {
        throw ScriptAbortException()
    }
}

/**
 * Clicks on the [Location].
 *
 * @param Times the amount of times to click
 */
fun Location.click(Times: Int = 1) {
    checkExitRequested()
    AutomataApi.GestureService?.click(this.transform(), Times)
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
    Similarity: Double = AutomataApi.MinSimilarity
): Boolean {
    checkExitRequested()
    return AutomataApi.checkConditionLoop(
        { AutomataApi.existsNow(this, Image, Similarity) },
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
    Similarity: Double = AutomataApi.MinSimilarity
): Boolean {
    checkExitRequested()
    return AutomataApi.checkConditionLoop(
        { !AutomataApi.existsNow(this, Image, Similarity) },
        Timeout
    )
}

/**
 * Clicks on the center of this Region.
 */
fun Region.click() = center.click()

/**
 * Gets the width and height in the form of a [Size] object.
 */
val IPattern.Size get() = Size(width, height)

/**
 * Adds borders around the [Region].
 *
 * @param Duration how long the borders should be displayed
 */
fun Region.highlight(Duration: Duration = 0.3.seconds) {
    checkExitRequested()
    AutomataApi.PlatformImpl?.highlight(this.transform(), Duration)
}

/**
 * Gets the image content of this Region.
 *
 * @return an [IPattern] object with the image data
 */
fun Region.getPattern(): IPattern? {
    return ScreenshotManager.getScreenshot()
        ?.crop(this.transformToImage())
        ?.copy()
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
    Similarity: Double = AutomataApi.MinSimilarity
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

/**
 * Central class used for triggering gestures and image recognition.
 */
class AutomataApi {
    companion object {
        var PlatformImpl: IPlatformImpl? = null
        var GestureService: IGestureService? = null

        fun registerPlatform(Impl: IPlatformImpl) {
            PlatformImpl = Impl
        }

        fun registerGestures(Impl: IGestureService) {
            GestureService = Impl
        }

        /**
         * The default minimum similarity used for image comparisons.
         */
        const val MinSimilarity = 0.8

        fun loadPattern(Stream: InputStream): IPattern {
            return PlatformImpl!!.loadPattern(Stream)
        }

        fun getResizableBlankPattern(): IPattern {
            return PlatformImpl!!.getResizableBlankPattern()
        }

        /**
         * Wait for a given [Duration]. The wait is paused regularly to check if the stop button has
         * been pressed.
         */
        fun wait(Duration: Duration) {
            val epsilon = 1000L
            var left = Duration.toLongMilliseconds()

            // Sleeping this way allows quick exit if demanded by user
            while (left > 0) {
                checkExitRequested()

                val toSleep = min(epsilon, left)
                Thread.sleep(toSleep)
                left -= toSleep
            }
        }

        @Volatile
        var exitRequested = false

        val WindowRegion: Region get() = PlatformImpl!!.windowRegion

        /**
         * Shows a message box with the given title and message.
         */
        fun showMessageBox(Title: String, Message: String, Error: Exception? = null) {
            PlatformImpl?.messageBox(Title, Message, Error)
        }

        /**
         * Shows a toast with the given message.
         */
        fun toast(Message: String) {
            PlatformImpl?.toast(Message)
        }

        /**
         * Checks if the [Region] contains the provided image.
         *
         * @param Region the search region
         * @param Image the image to look for
         * @param Similarity the minimum similarity for this search
         */
        fun existsNow(
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
        fun checkConditionLoop(
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
                    wait(timeToWait)
                }
            }

            return false
        }

        /**
         * Takes a screenshot and caches it for the duration of the function invocation. This is
         * useful when you want to reuse the same screenshot for multiple image searches.
         *
         * @param Action a function to invoke which will use the cached screenshot
         */
        fun <T> useSameSnapIn(Action: () -> T): T {
            ScreenshotManager.snapshot()

            try {
                return Action()
            } finally {
                ScreenshotManager.usePreviousSnap = false
            }
        }

        /**
         * Swipes from one [Location] to another [Location].
         *
         * @param Start the [Location] where the swipe should start
         * @param End the [Location] where the swipe should end
         */
        fun swipe(Start: Location, End: Location) {
            GestureService?.swipe(Start.transform(), End.transform())
        }
    }
}