package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.Duration

class AutomataApi @Inject constructor(
    private val screenshotManager: ScreenshotManager,
    gestureExtensions: IGestureExtensions,
    private val highlight: Highlighter,
    imageMatchingExtensions: IImageMatchingExtensions,
    transformationExtensions: ITransformationExtensions,
    private val colorManager: ColorManager,
    private val wait: Waiter
) : IAutomataExtensions,
    IGestureExtensions by gestureExtensions,
    IImageMatchingExtensions by imageMatchingExtensions,
    ITransformationExtensions by transformationExtensions {

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
}

