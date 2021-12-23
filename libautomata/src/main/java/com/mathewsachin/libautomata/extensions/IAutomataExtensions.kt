package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Pattern
import com.mathewsachin.libautomata.Region
import kotlin.time.Duration

interface IAutomataExtensions : IImageMatchingExtensions {
    /**
     * Gets the image content of this Region.
     *
     * @return an [Pattern] object with the image data
     */
    fun Region.getPattern(): Pattern

    fun <T> useSameSnapIn(block: () -> T): T

    fun <T> useColor(block: () -> T): T

    fun Duration.wait()

    fun Location.click(times: Int = 1)

    fun Region.click(times: Int = 1)
}