package com.mathewsachin.libautomata

/**
 * Interface for classes which can take screenshots.
 */
interface IScreenshotService : AutoCloseable {
    /**
     * Takes a screenshot.
     *
     * @return an [IPattern] with the image data
     */
    fun takeScreenshot(): IPattern

    /**
     * Starts recording
     *
     * @return [AutoCloseable] which can be closed to stop recording, or null if recording is not supported
     */
    fun startRecording(): AutoCloseable?
}