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

    /**
     * Swipes from one [Location] to another [Location].
     *
     * @param start the [Location] where the swipe should start
     * @param end the [Location] where the swipe should end
     */
    fun swipe(start: Location, end: Location)
}