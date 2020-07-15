package com.mathewsachin.libautomata

import com.mathewsachin.libautomata.ExitManager.checkExitRequested
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.seconds

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
    AutomataApi.PlatformImpl.highlight(this.transform(), Duration)
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
 * Wait for a given [Duration]. The wait is paused regularly to check if the stop button has
 * been pressed.
 */
fun Duration.wait() {
    val epsilon = 1000L
    val multiplier = AutomataApi.PlatformImpl.prefs.waitMultiplier
    var left = (this * multiplier).toLongMilliseconds()

    // Sleeping this way allows quick exit if demanded by user
    while (left > 0) {
        checkExitRequested()

        val toSleep = min(epsilon, left)
        Thread.sleep(toSleep)
        left -= toSleep
    }
}

object AutomataApi {
    lateinit var PlatformImpl: IPlatformImpl

    fun registerPlatform(Impl: IPlatformImpl) {
        PlatformImpl = Impl
    }
}