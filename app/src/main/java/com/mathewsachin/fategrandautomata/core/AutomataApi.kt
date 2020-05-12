package com.mathewsachin.fategrandautomata.core

import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import java.io.InputStream
import kotlin.math.min

fun checkExitRequested() {
    if (AutomataApi.exitRequested) {
        // Reset exit requested
        AutomataApi.exitRequested = false

        throw ScriptAbortException()
    }
}

fun Location.click() {
    checkExitRequested()
    AutomataApi.GestureService?.click(this.transform())
}

fun Location.continueClick(Times: Int) {
    checkExitRequested()
    AutomataApi.GestureService?.continueClick(this.transform(), Times)
}

fun Region.exists(Image: IPattern, TimeoutSeconds: Double = 0.0, Similarity: Double = AutomataApi.MinSimilarity): Boolean {
    checkExitRequested()
    return AutomataApi.checkConditionLoop({ AutomataApi.existsNow(this, Image, Similarity) }, TimeoutSeconds)
}

fun Region.waitVanish(Image: IPattern, TimeoutSeconds: Double = 0.0, Similarity: Double = AutomataApi.MinSimilarity): Boolean {
    checkExitRequested()
    return AutomataApi.checkConditionLoop({ !AutomataApi.existsNow(this, Image, Similarity) }, TimeoutSeconds)
}

fun Region.click() = center.click()

val IPattern.Size get() = Size(width, height)

fun Region.highlight(Seconds: Double = AutomataApi.DefaultHighlightTimeoutSeconds) {
    checkExitRequested()
    AutomataApi.PlatformImpl?.highlight(this.transform(), Seconds)
}

fun Region.getPattern(): IPattern? {
    return ScreenshotManager.getScreenshot()
        ?.crop(this.transformToImage())
        ?.copy()
}

fun Region.findAll(Pattern: IPattern, Similarity: Double = AutomataApi.MinSimilarity): Sequence<Match> {
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

            region = region.copy(X = region.X + this.X, Y = region.Y + this.Y)

            Match(region, it.score)
        }
}

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

        var MinSimilarity = 0.8

        var DefaultHighlightTimeoutSeconds = 0.3

        fun loadPattern(Stream: InputStream): IPattern {
            return PlatformImpl!!.loadPattern(Stream)
        }

        fun getResizableBlankPattern(): IPattern {
            return PlatformImpl!!.getResizableBlankPattern()
        }

        fun wait(Seconds: Double) {
            val epsilon = 1000L
            var left = (Seconds * 1000).toLong()

            // Sleeping this way allows quick exit if demanded by user
            while (left > 0) {
                checkExitRequested()

                val toSleep = min(epsilon, left)
                Thread.sleep(toSleep)
                left -= toSleep
            }
        }

        fun wait(Seconds: Int) {
            wait(Seconds.toDouble())
        }

        @Volatile var exitRequested = false

        val WindowRegion: Region get() = PlatformImpl!!.windowRegion

        fun showMessageBox(Title: String, Message: String) {
            PlatformImpl?.messageBox(Title, Message)
        }

        fun toast(Message: String) {
            PlatformImpl?.toast(Message)
        }

        fun existsNow(Region: Region, Image: IPattern, Similarity: Double = MinSimilarity): Boolean {
            checkExitRequested()

            var sshot = ScreenshotManager.getScreenshot()

            if (Preferences.DebugMode) {
                Region.highlight()
            }

            sshot = sshot?.crop(Region.transformToImage())

            return sshot?.isMatch(Image, Similarity) ?: false
        }

        const val ScanRate: Int = 3

        private val stopwatch = Stopwatch()

        fun checkConditionLoop(Condition: () -> Boolean, TimeoutSeconds: Double = 0.0): Boolean {
            stopwatch.start()

            while (true) {
                val scanStartMs = stopwatch.elapsedMs

                if (Condition.invoke()) {
                    return true
                }

                if (TimeoutSeconds == 0.0 || stopwatch.elapsedSec > TimeoutSeconds) {
                    break
                }

                val scanIntervalMs = 1000.0 / ScanRate
                val elapsedMs = stopwatch.elapsedMs - scanStartMs
                val timeToWaitMs = scanIntervalMs - elapsedMs

                if (timeToWaitMs > 0) {
                    wait(timeToWaitMs / 1000.0)
                }
            }

            return false
        }

        fun <T> useSameSnapIn(Action: () -> T): T {
            ScreenshotManager.snapshot()

            try {
                return Action()
            }
            finally {
                ScreenshotManager.usePreviousSnap = false
            }
        }

        fun swipe(Start: Location, End: Location) {
            GestureService?.swipe(Start.transform(), End.transform())
        }
    }
}