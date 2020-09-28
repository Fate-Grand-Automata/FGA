package com.mathewsachin.fategrandautomata.util

import android.os.Build
import com.mathewsachin.fategrandautomata.scripts.ISwipeLocations
import com.mathewsachin.libautomata.Location
import javax.inject.Inject

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
            Location(1400, if (isNewSwipeMethod()) 400 else 575)
        )
}