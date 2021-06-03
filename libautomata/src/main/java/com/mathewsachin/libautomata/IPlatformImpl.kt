package com.mathewsachin.libautomata

import kotlin.time.Duration

interface IPlatformImpl {
    val windowRegion: Region
    val canLongSwipe: Boolean
    val prefs: IPlatformPrefs

    /**
     * Shows a toast with the given message.
     */
    fun toast(Message: String)

    fun notify(message: String)

    /**
     * Creates a new [IPattern] without any image data.
     */
    fun getResizableBlankPattern(): IPattern

    /**
     * Adds borders around the given [Region].
     *
     * @param Region a [Region] on the screen
     * @param Duration how long the borders should be displayed
     */
    fun highlight(Region: Region, Duration: Duration, success: Boolean)
}