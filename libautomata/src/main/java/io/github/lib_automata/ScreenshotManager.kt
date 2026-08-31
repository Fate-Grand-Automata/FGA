package io.github.lib_automata

import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

/**
 * Responsible for taking screenshots via a [ScreenshotService]. The screenshots are
 * scaled and cropped and can be cached for a while using [snapshot].
 */
@ScriptScope
class ScreenshotManager @Inject constructor(
    private val gameAreaManager: GameAreaManager,
    private val screenshotService: ScreenshotService,
    private val scale: Scale
) : AutoCloseable {
    var usePreviousSnap = false

    private var previousPattern: Pattern? = null

    /**
     * Takes a screenshot, crops it to the game area and then scales it to the image scale so
     * it can be used for image comparisons.
     */
    private fun getScaledScreenshot(): Pattern =
        screenshotService.takeScreenshot()
            .crop(gameAreaManager.gameArea * (scale.screenToImage ?: 1.0))

    /**
     * Takes a screenshot and sets [usePreviousSnap] to `true`. All following [getScreenshot]
     * calls will use the same screenshot, until [usePreviousSnap] is set to `false` again.
     *
     * The screenshot image can be retrieved using [getScreenshot].
     */
    fun snapshot() {
        keepAsPrevious(getScaledScreenshot())
        usePreviousSnap = true
    }

    /**
     * [getScaledScreenshot] crops, and a crop is a fresh native image that has to be released.
     * Nothing hands the previous one back out - callers use it within the expression they got
     * it from, and [AutomataApi.getPattern] clones it - so it can be dropped here.
     */
    private fun keepAsPrevious(pattern: Pattern): Pattern {
        previousPattern?.close()
        previousPattern = pattern

        return pattern
    }

    /**
     * Takes a screenshot, crops it to the game area and then scales it to the image scale so
     * it can be used for image comparisons.
     *
     * If [usePreviousSnap] is set to true, a cached screenshot is returned instead.
     *
     * @return an [Pattern] with the screenshot image data
     */
    fun getScreenshot(): Pattern {
        if (usePreviousSnap) {
            previousPattern?.let { return it }
        }

        return keepAsPrevious(getScaledScreenshot())
    }

    /**
     * Takes a screenshot and caches it for the duration of the function invocation. This is
     * useful when you want to reuse the same screenshot for multiple image searches.
     *
     * @param Action a function to invoke which will use the cached screenshot
     */
    fun <T> useSameSnapIn(Action: () -> T): T {
        snapshot()

        try {
            return Action()
        } finally {
            usePreviousSnap = false
        }
    }

    /**
     * Releases the memory reserved for the cached screenshot and helper images.
     */
    override fun close() {
        previousPattern?.close()
        previousPattern = null
    }
}