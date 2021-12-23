package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

interface IGestureExtensions {
    /**
     * Clicks on the [Location].
     *
     * @param times the amount of times to click
     */
    fun Location.click(times: Int = 1)

    /**
     * Clicks on the center of this Region.
     *
     * @param times the amount of times to click
     */
    fun Region.click(times: Int = 1)
}