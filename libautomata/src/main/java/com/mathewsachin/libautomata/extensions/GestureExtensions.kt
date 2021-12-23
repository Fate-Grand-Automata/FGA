package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.*
import javax.inject.Inject

class GestureExtensions @Inject constructor(
    val gestureService: GestureService,
    val exitManager: ExitManager,
    val platformImpl: PlatformImpl,
    transformationExtensions: ITransformationExtensions
) : IGestureExtensions, ITransformationExtensions by transformationExtensions {
    override fun Location.click(times: Int) {
        exitManager.checkExitRequested()
        gestureService.click(this.transform(), times)
    }

    override fun Region.click(times: Int) = center.click(times)

    override fun swipe(start: Location, end: Location) {
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
            start.transform(),
            Location(endX, endY).transform()
        )
    }

    /**
     * linear interpolation
     */
    private fun lerp(start: Int, end: Int, fraction: Double) =
        (start + (end - start) * fraction).toInt()
}