package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.ColorManager
import com.mathewsachin.libautomata.HighlightColor
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.ScreenshotManager
import javax.inject.Inject

class AutomataApi @Inject constructor(
    private val screenshotManager: ScreenshotManager,
    durationExtensions: IDurationExtensions,
    gestureExtensions: IGestureExtensions,
    highlightExtensions: IHighlightExtensions,
    imageMatchingExtensions: IImageMatchingExtensions,
    transformationExtensions: ITransformationExtensions,
    private val colorManager: ColorManager
) : IAutomataExtensions,
    IDurationExtensions by durationExtensions,
    IGestureExtensions by gestureExtensions,
    IHighlightExtensions by highlightExtensions,
    IImageMatchingExtensions by imageMatchingExtensions,
    ITransformationExtensions by transformationExtensions {

    override fun Region.getPattern() =
        screenshotManager.getScreenshot()
            .crop(this.transformToImage())
            .also { highlight(HighlightColor.Info) }
            .copy() // It is important that the image gets cloned here.

    override fun <T> useSameSnapIn(block: () -> T) =
        screenshotManager.useSameSnapIn(block)

    override fun <T> useColor(block: () -> T): T =
        colorManager.useColor(block)
}

