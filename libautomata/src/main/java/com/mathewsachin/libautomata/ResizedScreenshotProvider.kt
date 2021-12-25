package com.mathewsachin.libautomata

class ResizedScreenshotProvider(
    private val original: ScreenshotService,
    private val scale: Double,
    private val platformImpl: PlatformImpl
): ScreenshotService {
    private val resizeTarget: Pattern by lazy { platformImpl.getResizableBlankPattern() }

    override fun takeScreenshot(): Pattern {
        val shot = original.takeScreenshot()
        shot.resize(resizeTarget, shot.size * scale)
        return resizeTarget
    }

    override fun startRecording() = original.startRecording()

    override fun close() {
        original.close()
        resizeTarget.close()
    }
}