package com.mathewsachin.fategrandautomata.core

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
}