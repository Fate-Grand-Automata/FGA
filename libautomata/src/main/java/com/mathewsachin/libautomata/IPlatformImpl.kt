package com.mathewsachin.libautomata

import kotlin.time.Duration

interface IPlatformImpl {
    val windowRegion: Region
    val canLongSwipe: Boolean
    val prefs: IPlatformPrefs

    /**
     * Creates a new [IPattern] without any image data.
     */
    fun getResizableBlankPattern(): IPattern

    /**
     * Adds borders around the given [Region].
     *
     * @param region a [Region] on the screen
     * @param duration how long the borders should be displayed
     */
    fun highlight(region: Region, duration: Duration, color: HighlightColor)
}