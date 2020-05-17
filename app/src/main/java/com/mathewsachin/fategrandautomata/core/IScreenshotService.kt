package com.mathewsachin.fategrandautomata.core

interface IScreenshotService : AutoCloseable {
    /**
     * Takes a screenshot.
     *
     * @return an [IPattern] with the image data
     */
    fun takeScreenshot(): IPattern
}