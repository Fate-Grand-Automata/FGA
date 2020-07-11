package com.mathewsachin.libautomata

import kotlin.math.min
import kotlin.time.Duration

interface IDurationExtensions {
    /**
     * Wait for a given [Duration]. The wait is paused regularly to check if the stop button has
     * been pressed.
     */
    fun Duration.wait()
}

class DurationExtensions(
    val exitManager: ExitManager,
    val sleeper: (Long) -> Unit = { Thread.sleep(it) }
) : IDurationExtensions {
    /**
     * Wait for a given [Duration]. The wait is paused regularly to check if the stop button has
     * been pressed.
     */
    override fun Duration.wait() {
        val epsilon = 1000L
        var left = this.toLongMilliseconds()

        // Sleeping this way allows quick exit if demanded by user
        while (left > 0) {
            exitManager.checkExitRequested()

            val toSleep = min(epsilon, left)
            sleeper(toSleep)
            left -= toSleep
        }
    }
}