package com.mathewsachin.libautomata

/**
 * A static class responsible for taking screenshots via a [IScreenshotService]. The screenshots are
 * scaled and cropped and can be cached for a while using [snapshot].
 */
object ScreenshotManager {
    private var impl: IScreenshotService? = null

    fun register(Impl: IScreenshotService) {
        impl = Impl
    }

    var usePreviousSnap = false

    private var previousPattern: IPattern? = null
    private var resizeTarget: IPattern? = null

    /**
     * Takes a screenshot, crops it to the game area and then scales it to the image scale so
     * it can be used for image comparisons.
     */
    private fun getScaledScreenshot(): IPattern {
        val sshot = impl!!.takeScreenshot()
            .crop(GameAreaManager.GameArea)

        val scale = screenToImageScale()

        if (scale != null) {
            if (resizeTarget == null) {
                resizeTarget = AutomataApi.PlatformImpl.getResizableBlankPattern()
            }

            sshot.resize(resizeTarget!!, sshot.Size * scale)

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
     * @return an [IPattern] with the screenshot image data
     */
    fun getScreenshot(): IPattern? {
        if (usePreviousSnap) {
            return previousPattern
        }

        previousPattern = getScaledScreenshot()
        return previousPattern
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
    fun releaseMemory() {
        previousPattern?.close()
        previousPattern = null

        resizeTarget?.close()
        resizeTarget = null
    }
}