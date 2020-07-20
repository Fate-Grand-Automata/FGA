package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.IPlatformImpl
import javax.inject.Inject
import kotlin.math.min
import kotlin.time.Duration

class DurationExtensions @Inject constructor(
    val platformImpl: IPlatformImpl,
    val exitManager: ExitManager
) : IDurationExtensions {
    override fun Duration.wait() {
        val epsilon = 1000L
        val multiplier = platformImpl.prefs.waitMultiplier
        var left = (this * multiplier).toLongMilliseconds()

        // Sleeping this way allows quick exit if demanded by user
        while (left > 0) {
            exitManager.checkExitRequested()

            val toSleep = min(epsilon, left)
            Thread.sleep(toSleep)
            left -= toSleep
        }
    }
}