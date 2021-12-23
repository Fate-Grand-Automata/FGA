package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.Pattern
import com.mathewsachin.libautomata.Region

interface IAutomataExtensions : IDurationExtensions,
    IGestureExtensions,
    IHighlightExtensions,
    IImageMatchingExtensions,
    ITransformationExtensions {
    /**
     * Gets the image content of this Region.
     *
     * @return an [Pattern] object with the image data
     */
    fun Region.getPattern(): Pattern

    fun <T> useSameSnapIn(block: () -> T): T

    fun <T> useColor(block: () -> T): T
}