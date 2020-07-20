package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.ScreenshotManager

interface IAutomataExtensions : IDurationExtensions,
    IGestureExtensions,
    IHighlightExtensions,
    IImageMatchingExtensions,
    ITransformationExtensions {
    /**
     * Gets the image content of this Region.
     *
     * @return an [IPattern] object with the image data
     */
    fun Region.getPattern(): IPattern?

    val screenshotManager: ScreenshotManager

    fun toast(msg: String)
}