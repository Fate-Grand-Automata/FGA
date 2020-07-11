package com.mathewsachin.libautomata

interface IAutomataExtensions :
    IDurationExtensions,
    IHighlightExtensions,
    IImageMatchingExtensions,
    IGestureExtensions
{
    /**
     * Gets the image content of this Region.
     *
     * @return an [IPattern] object with the image data
     */
    fun Region.getPattern(): IPattern?
}

class AutomataExtensions(
    val screenshotManager: ScreenshotManager,
    durationExtensions: IDurationExtensions,
    highlightExtensions: IHighlightExtensions,
    imageMatchingExtensions: IImageMatchingExtensions,
    gestureExtensions: IGestureExtensions,
    transformationExtensions: ITransformationExtensions) :
        IAutomataExtensions,
        IDurationExtensions by durationExtensions,
        IHighlightExtensions by highlightExtensions,
        IImageMatchingExtensions by imageMatchingExtensions,
        IGestureExtensions by gestureExtensions,
        ITransformationExtensions by transformationExtensions
{
    /**
     * Gets the image content of this Region.
     *
     * @return an [IPattern] object with the image data
     */
    override fun Region.getPattern(): IPattern? {
        return screenshotManager.getScreenshot()
            ?.crop(this.transformToImage())
            ?.copy()
    }
}