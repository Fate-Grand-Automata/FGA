package com.mathewsachin.fategrandautomata.root

import com.mathewsachin.fategrandautomata.core.IGestureService
import com.mathewsachin.fategrandautomata.core.Location
import com.mathewsachin.fategrandautomata.util.swipeDuration
import kotlin.math.max

private const val InputCommand = "/system/bin/input"

class RootGestures(private val SuperUser: SuperUser) : IGestureService {
    override fun swipe(Start: Location, End: Location) {
        SuperUser.sendCommand("$InputCommand swipe ${Start.X} ${Start.Y} ${End.X} ${End.Y} $swipeDuration")
    }

    override fun click(Location: Location, Times: Int) {
        var times = max(1, Times / 5)
        val clickDuration = 1

        repeat(times) {
            //_superUser.SendCommand($"{InputCommand} tap {Location.X} {Location.Y}");
            SuperUser.sendCommand("$InputCommand swipe ${Location.X} ${Location.Y} ${Location.X} ${Location.Y} $clickDuration")
        }
    }

    override fun close() {}
}