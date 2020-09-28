package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.libautomata.Location

interface ISwipeLocations {
    data class SwipeLocation(val start: Location, val end: Location)

    val supportList: SwipeLocation
    val giftBox: SwipeLocation
}