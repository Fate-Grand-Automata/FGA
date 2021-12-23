package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.Duration

class AutomataApi @Inject constructor(
    private val screenshotManager: ScreenshotManager,
    private val highlight: Highlighter,
    private val click: Clicker,
    imageMatchingExtensions: IImageMatchingExtensions,
    transformations: ITransformationExtensions,
    private val colorManager: ColorManager,
    private val wait: Waiter
) : IAutomataExtensions,
    IImageMatchingExtensions by imageMatchingExtensions,
    ITransformationExtensions by transformations {

    override fun Region.getPattern() =
        screenshotManager.getScreenshot()
            .crop(this.transformToImage())
            .also { highlight(this, HighlightColor.Info) }
            .copy() // It is important that the image gets cloned here.

    override fun <T> useSameSnapIn(block: () -> T) =
        screenshotManager.useSameSnapIn(block)

    override fun <T> useColor(block: () -> T): T =
        colorManager.useColor(block)

    override fun Duration.wait() = wait(this)

    override fun Location.click(times: Int) = click(this, times)

    override fun Region.click(times: Int) = center.click(times)
}

