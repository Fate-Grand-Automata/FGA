package com.mathewsachin.libautomata

import java.io.InputStream
import kotlin.time.Duration

interface IPlatformImpl {
    val windowRegion: Region

    val debugMode: Boolean

    /**
     * Shows a toast with the given message.
     */
    fun toast(Message: String)

    /**
     * Loads an image from the given [InputStream].
     *
     * @return an [IPattern] with the image data
     */
    fun loadPattern(Stream: InputStream): IPattern

    /**
     * Creates a new [IPattern] without any image data.
     */
    fun getResizableBlankPattern(): IPattern

    /**
     * Shows a message box with the given title and message.
     */
    fun messageBox(Title: String, Message: String, Error: Exception? = null)

    /**
     * Adds borders around the given [Region].
     *
     * @param Region a [Region] on the screen
     * @param Duration how long the borders should be displayed
     */
    fun highlight(Region: Region, Duration: Duration)
}