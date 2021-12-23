package com.mathewsachin.libautomata

import javax.inject.Inject

interface Swiper {
    /**
     * Swipes from one [Location] to another [Location].
     *
     * @param start the [Location] where the swipe should start
     * @param end the [Location] where the swipe should end
     */
    operator fun invoke(start: Location, end: Location)
}

class RealSwiper @Inject constructor(
    private val gestureService: GestureService,
    private val platformImpl: PlatformImpl,
    private val transform: Transformer
): Swiper {
    override fun invoke(start: Location, end: Location) {
        val endX = lerp(
            start.x,
            end.x,
            platformImpl.prefs.swipeMultiplier
        ).coerceAtLeast(0)

        val endY = lerp(
            start.y,
            end.y,
            platformImpl.prefs.swipeMultiplier
        ).coerceAtLeast(0)

        gestureService.swipe(
            transform.toScreen(start),
            transform.toScreen(Location(endX, endY))
        )
    }

    /**
     * linear interpolation
     */
    private fun lerp(start: Int, end: Int, fraction: Double) =
        (start + (end - start) * fraction).toInt()
}