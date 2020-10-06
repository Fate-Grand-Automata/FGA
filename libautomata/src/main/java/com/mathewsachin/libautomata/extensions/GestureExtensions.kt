package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.*
import javax.inject.Inject

class GestureExtensions @Inject constructor(
    val gestureService: IGestureService,
    val exitManager: ExitManager,
    val platformImpl: IPlatformImpl,
    transformationExtensions: ITransformationExtensions
) : IGestureExtensions, ITransformationExtensions by transformationExtensions {
    override fun Location.click(Times: Int) {
        exitManager.checkExitRequested()
        gestureService.click(this.transform(), Times)
    }

    override fun Region.click(Times: Int) = center.click(Times)

    override fun swipe(Start: Location, End: Location) {
        val endX = lerp(
            Start.X,
            End.X,
            platformImpl.prefs.swipeMultiplier
        ).coerceAtLeast(0)

        val endY = lerp(
            Start.Y,
            End.Y,
            platformImpl.prefs.swipeMultiplier
        ).coerceAtLeast(0)

        gestureService.swipe(
            Start.transform(),
            Location(endX, endY).transform()
        )
    }

    /**
     * linear interpolation
     */
    private fun lerp(start: Int, end: Int, fraction: Double) =
        (start + (end - start) * fraction).toInt()
}