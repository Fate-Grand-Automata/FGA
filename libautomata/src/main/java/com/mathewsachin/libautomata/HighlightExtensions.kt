package com.mathewsachin.libautomata

import kotlin.time.Duration
import kotlin.time.seconds

interface IHighlightExtensions {
    /**
     * Adds borders around the [Region].
     *
     * @param Duration how long the borders should be displayed
     */
    fun Region.highlight(Duration: Duration = 0.3.seconds)
}

class HighlightExtensions(
    val exitManager: ExitManager,
    val platformImpl: IPlatformImpl,
    transformationExtensions: ITransformationExtensions
): IHighlightExtensions, ITransformationExtensions by transformationExtensions {
    /**
     * Adds borders around the [Region].
     *
     * @param Duration how long the borders should be displayed
     */
    override fun Region.highlight(Duration: Duration) {
        exitManager.checkExitRequested()

        platformImpl.highlight(this.transform(), Duration)
    }
}