package com.mathewsachin.libautomata

class ResizedScreenshotProvider(
    private val original: ScreenshotService,
    private val scale: Double,
    platformImpl: PlatformImpl
) : ScreenshotService {
    private val resizeTarget = platformImpl.getResizableBlankPattern()

    override fun takeScreenshot(): Pattern {
        val shot = original.takeScreenshot()
        shot.resize(resizeTarget, shot.size * scale)
        return resizeTarget
    }

    override fun close() {
        original.close()
        resizeTarget.close()
    }
}