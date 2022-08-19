package com.mathewsachin.libautomata

import javax.inject.Inject
import kotlin.time.Duration

class StandardAutomataApi @Inject constructor(
    private val screenshotManager: ScreenshotManager,
    private val highlight: Highlighter,
    private val click: Clicker,
    private val imageMatcher: ImageMatcher,
    private val transform: Transformer,
    private val colorManager: ColorManager,
    private val wait: Waiter
) : AutomataApi {

    override fun Region.getPattern() =
        screenshotManager.getScreenshot()
            .crop(transform.toImage(this))
            .also { highlight(this, HighlightColor.Info) }
            .copy() // It is important that the image gets cloned here.

    override fun <T> useSameSnapIn(block: () -> T) =
        screenshotManager.useSameSnapIn(block)

    override fun <T> useColor(block: () -> T): T =
        colorManager.useColor(block)

    override fun Duration.wait() = wait(this)

    override fun Location.click(times: Int) = click(this, times)

    override fun Region.exists(
        image: Pattern,
        timeout: Duration,
        similarity: Double?
    ) = imageMatcher.exists(this, image, timeout, similarity)

    override fun Region.waitVanish(
        image: Pattern,
        timeout: Duration,
        similarity: Double?
    ) = imageMatcher.waitVanish(this, image, timeout, similarity)

    override fun Region.findAll(
        pattern: Pattern,
        similarity: Double?
    ) = imageMatcher.findAll(this, pattern, similarity)

    override fun Region.isWhite() = imageMatcher.isWhite(this)
}

