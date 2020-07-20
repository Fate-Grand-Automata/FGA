package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.ScreenshotManager
import javax.inject.Inject

class AutomataApi @Inject constructor(
    override val screenshotManager: ScreenshotManager,
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
            ?.crop(this.transformToImage())
            ?.copy()

    override fun toast(msg: String) = platformImpl.toast(msg)
}

