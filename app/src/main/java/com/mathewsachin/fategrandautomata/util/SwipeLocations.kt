package com.mathewsachin.fategrandautomata.util

import android.os.Build
import com.mathewsachin.fategrandautomata.scripts.ISwipeLocations
import com.mathewsachin.libautomata.Location
import javax.inject.Inject

/**
 * Android 8 added support for continued gestures, so we can do precise swiping.
 * On Android 7, long swipes cause weird behaviour, so we need different locations.
 */
class SwipeLocations @Inject constructor() : ISwipeLocations {
    fun isNewSwipeMethod() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    override val supportList: ISwipeLocations.SwipeLocation
        get() = ISwipeLocations.SwipeLocation(
            Location(35, if (isNewSwipeMethod()) 1000 else 1190),
            Location(5, if (isNewSwipeMethod()) 300 else 660)
        )

    override val giftBox: ISwipeLocations.SwipeLocation
        get() = ISwipeLocations.SwipeLocation(
            Location(1400, if (isNewSwipeMethod()) 1200 else 1050),
            Location(1400, if (isNewSwipeMethod()) 350 else 575)
        )
}