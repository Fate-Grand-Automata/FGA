package com.mathewsachin.libautomata.extensions

import kotlin.time.Duration

interface IDurationExtensions {
    /**
     * Wait for a given [Duration]. The wait is paused regularly to check if the stop button has
     * been pressed.
     */
    fun Duration.wait()
}