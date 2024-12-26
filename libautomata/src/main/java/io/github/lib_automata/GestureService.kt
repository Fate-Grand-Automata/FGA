package io.github.lib_automata

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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


    fun longPress(location: Location, duration: Duration = 2.seconds)
}