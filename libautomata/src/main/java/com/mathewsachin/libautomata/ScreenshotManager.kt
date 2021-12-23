package com.mathewsachin.libautomata

import com.mathewsachin.libautomata.dagger.ScriptScope
import com.mathewsachin.libautomata.extensions.ITransformationExtensions
import javax.inject.Inject

/**
 * Responsible for taking screenshots via a [ScreenshotService]. The screenshots are
 * scaled and cropped and can be cached for a while using [snapshot].
 */
@ScriptScope
class ScreenshotManager @Inject constructor(
    val gameAreaManager: GameAreaManager,
    val screenshotService: ScreenshotService,
    val platformImpl: PlatformImpl,
    val transformationExtensions: ITransformationExtensions
) : AutoCloseable {
    var usePreviousSnap = false

    private var previousPattern: Pattern? = null
    private var resizeTarget: Pattern? = null

    /**
     * Takes a screenshot, crops it to the game area and then scales it to the image scale so
     * it can be used for image comparisons.
     */
    private fun getScaledScreenshot(): Pattern {
        val sshot = screenshotService.takeScreenshot()
            .crop(gameAreaManager.gameArea)

        val scale = transformationExtensions.screenToImageScale()

        if (scale != null) {
            if (resizeTarget == null) {
                resizeTarget = platformImpl.getResizableBlankPattern()
            }

            sshot.resize(resizeTarget!!, sshot.size * scale)

            return resizeTarget!!
        }

        return sshot
    }

    /**
     * Takes a screenshot and sets [usePreviousSnap] to `true`. All following [getScreenshot]
     * calls will use the same screenshot, until [usePreviousSnap] is set to `false` again.
     *
     * The screenshot image can be retrieved using [getScreenshot].
     */
    fun snapshot() {
        previousPattern = getScaledScreenshot()
        usePreviousSnap = true
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

        return getScaledScreenshot().also {
            previousPattern = it
        }
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

        resizeTarget?.close()
        resizeTarget = null
    }
}