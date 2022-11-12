package com.mathewsachin.libautomata

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
}