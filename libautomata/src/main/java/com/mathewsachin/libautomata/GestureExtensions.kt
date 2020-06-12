package com.mathewsachin.libautomata

import com.mathewsachin.libautomata.ExitManager.checkExitRequested

private var GestureService: IGestureService? = null

fun registerGestures(Impl: IGestureService) {
    GestureService = Impl
}

/**
 * Clicks on the [Location].
 *
 * @param Times the amount of times to click
 */
fun Location.click(Times: Int = 1) {
    checkExitRequested()
    GestureService?.click(this.transform(), Times)
}

/**
 * Clicks on the center of this Region.
 *
 * @param Times the amount of times to click
 */
fun Region.click(Times: Int = 1) = center.click(Times)

/**
 * Swipes from one [Location] to another [Location].
 *
 * @param Start the [Location] where the swipe should start
 * @param End the [Location] where the swipe should end
 */
fun swipe(Start: Location, End: Location) {
    GestureService?.swipe(Start.transform(), End.transform())
}