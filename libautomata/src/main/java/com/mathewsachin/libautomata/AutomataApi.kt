package com.mathewsachin.libautomata

import com.mathewsachin.libautomata.extensions.*

interface IAutomataExtensions : IDurationExtensions, IGestureExtensions, IHighlightExtensions,
    IImageMatchingExtensions, ITransformationExtensions {
    /**
     * Gets the image content of this Region.
     *
     * @return an [IPattern] object with the image data
     */
    fun Region.getPattern(): IPattern?

    val screenshotManager: ScreenshotManager
}

class AutomataApi(
    override val screenshotManager: ScreenshotManager,
    durationExtensions: IDurationExtensions,
    gestureExtensions: IGestureExtensions,
    highlightExtensions: IHighlightExtensions,
    imageMatchingExtensions: IImageMatchingExtensions,
    transformationExtensions: ITransformationExtensions
) : IAutomataExtensions,
    IDurationExtensions by durationExtensions,
    IGestureExtensions by gestureExtensions,
    IHighlightExtensions by highlightExtensions,
    IImageMatchingExtensions by imageMatchingExtensions,
    ITransformationExtensions by transformationExtensions {

    override fun Region.getPattern() =
        screenshotManager.getScreenshot()
            ?.crop(this.transformToImage())
            ?.copy()
}

