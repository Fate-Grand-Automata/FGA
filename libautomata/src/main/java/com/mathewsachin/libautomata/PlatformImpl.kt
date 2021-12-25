package com.mathewsachin.libautomata

import kotlin.time.Duration

interface PlatformImpl {
    val windowRegion: Region
    val canLongSwipe: Boolean
    val prefs: PlatformPrefs

    /**
     * Creates a new [Pattern] without any image data.
     */
    fun getResizableBlankPattern(): Pattern

    /**
     * Adds borders around the given [Region].
     *
     * @param region a [Region] on the screen
     * @param duration how long the borders should be displayed
     */
    fun highlight(region: Region, duration: Duration, color: HighlightColor)
}