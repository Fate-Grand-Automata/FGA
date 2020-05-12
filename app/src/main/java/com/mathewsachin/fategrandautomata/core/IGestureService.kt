package com.mathewsachin.fategrandautomata.core

interface IGestureService : AutoCloseable {
    fun swipe(Start: Location, End: Location)

    fun click(Location: Location)

    fun continueClick(Location: Location, Times: Int)
}