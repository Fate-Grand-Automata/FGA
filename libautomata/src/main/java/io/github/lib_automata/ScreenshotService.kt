package io.github.lib_automata

/**
 * Interface for classes which can take screenshots.
 */
interface ScreenshotService : AutoCloseable {
    /**
     * Takes a screenshot.
     *
     * @return an [Pattern] with the image data
     */
    fun takeScreenshot(): Pattern

    /**
     * Starts recording
     *
     * @return [AutoCloseable] which can be closed to stop recording, or null if recording is not supported
     */
    fun startRecording(): AutoCloseable?
}