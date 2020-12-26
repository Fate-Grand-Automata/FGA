package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.IPattern
import com.mathewsachin.libautomata.Region

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
    fun Region.getPattern(): IPattern

    fun <T> useSameSnapIn(block: () -> T): T

    fun takeColorScreenshot(): IPattern

    fun toast(msg: String)

    fun notify(msg: String)
}