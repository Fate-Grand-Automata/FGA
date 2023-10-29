package io.github.lib_automata

import javax.inject.Inject

interface LongPressAndSwipeOrMultipleClicks {
    operator fun invoke(clicks: List<Location>, window: Int)
}

class RealLongPressAndSwipeOrMultipleClicks @Inject constructor(
    private val gestureService: GestureService,
    private val platformImpl: PlatformImpl,
    private val transform: Transformer
) : LongPressAndSwipeOrMultipleClicks {

    override fun invoke(clicks: List<Location>, window: Int) {
        var secondToTheLastLocation = Location()

        val transformClicks = clicks.mapIndexed { index: Int, location: Location ->
            if (index != clicks.lastIndex){
                secondToTheLastLocation = location
                transform.toScreen(location)
            } else {
                val endX = lerp(
                    secondToTheLastLocation.x,
                    location.x,
                    platformImpl.prefs.swipeMultiplier
                ).coerceAtLeast(0)

                val endY = lerp(
                    secondToTheLastLocation.y,
                    location.y,
                    platformImpl.prefs.swipeMultiplier
                ).coerceAtLeast(0)

                transform.toScreen(Location(endX, endY))
            }
        }

        gestureService.longPressAndDragOrMultipleClicks(
            clicks=transformClicks,
            window=window
        )
    }

    /**
     * linear interpolation
     */
    private fun lerp(start: Int, end: Int, fraction: Double) =
        (start + (end - start) * fraction).toInt()
}