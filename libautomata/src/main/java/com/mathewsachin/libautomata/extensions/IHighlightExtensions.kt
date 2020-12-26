package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.Region
import kotlin.time.Duration
import kotlin.time.seconds

interface IHighlightExtensions {
    /**
     * Adds borders around the [Region].
     *
     * @param Duration how long the borders should be displayed
     */
    fun Region.highlight(Duration: Duration = 0.3.seconds, success: Boolean = false)
}