package com.mathewsachin.fategrandautomata.core

interface IGestureService : AutoCloseable {
    /**
     * Swipes from one [Location] to another [Location].
     *
     * @param Start the [Location] where the swipe should start
     * @param End the [Location] where the swipe should end
     */
    fun swipe(Start: Location, End: Location)

    /**
     * Clicks on a given [Location].
     *
     * @param Location the location to click on
     * @param Times the number of times to click
     */
    fun click(Location: Location, Times: Int = 1)
}