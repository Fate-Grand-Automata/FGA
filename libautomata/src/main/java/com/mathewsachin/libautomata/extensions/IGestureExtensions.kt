package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region

interface IGestureExtensions {
    /**
     * Clicks on the [Location].
     *
     * @param Times the amount of times to click
     */
    fun Location.click(Times: Int = 1)

    /**
     * Clicks on the center of this Region.
     *
     * @param Times the amount of times to click
     */
    fun Region.click(Times: Int = 1)

    /**
     * Swipes from one [Location] to another [Location].
     *
     * @param Start the [Location] where the swipe should start
     * @param End the [Location] where the swipe should end
     */
    fun swipe(Start: Location, End: Location)
}