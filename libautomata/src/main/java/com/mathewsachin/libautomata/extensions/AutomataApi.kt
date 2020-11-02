package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.IColorScreenshotProvider
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.ScreenshotManager
import javax.inject.Inject

class AutomataApi @Inject constructor(
    private val screenshotManager: ScreenshotManager,
    val platformImpl: IPlatformImpl,
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
            .crop(this.transformToImage())
            .copy() // It is important that the image gets cloned here.

    override fun <T> useSameSnapIn(block: () -> T) =
        screenshotManager.useSameSnapIn(block)

    override fun takeColorScreenshot() =
        if (screenshotManager.screenshotService is IColorScreenshotProvider)
            screenshotManager.screenshotService.takeColorScreenshot()
        else screenshotManager.getScreenshot().copy()

    override fun toast(msg: String) = platformImpl.toast(msg)

    override fun notify(msg: String) = platformImpl.notify(msg)
}

