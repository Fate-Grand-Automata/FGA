package com.mathewsachin.fategrandautomata.core

interface IScreenshotService : AutoCloseable {
    fun takeScreenshot(): IPattern
}