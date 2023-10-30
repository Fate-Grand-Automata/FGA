package io.github.lib_automata

/**
 * Interface for classes which can perform gestures.
 */
interface GestureService : AutoCloseable {
    /**
     * Swipes from one [Location] to another [Location].
     *
     * @param start the [Location] where the swipe should start
     * @param end the [Location] where the swipe should end
     */
    fun swipe(start: Location, end: Location)

    /**
     * Clicks on a given [location].
     *
     * @param location the location to click on
     * @param times the number of times to click
     */
    fun click(location: Location, times: Int = 1)

    /**
     * If the system is Android 8.0 or above
     * Long presses and Swipes from one [Location] to another [Location].
     *
     * Otherwise, it will perform multiple clicks.
     *
     * @param clicks the [Location]s where the swipe should start and end
     */
    fun longPressAndDragOrMultipleClicks(clicks: List<Location>, chunked: Int)
}