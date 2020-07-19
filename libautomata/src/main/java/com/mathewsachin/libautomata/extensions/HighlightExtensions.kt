package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.IPlatformImpl
import com.mathewsachin.libautomata.Region
import kotlin.time.Duration

class HighlightExtensions(
    val exitManager: ExitManager,
    val platformImpl: IPlatformImpl,
    transformationExtensions: ITransformationExtensions
) : IHighlightExtensions, ITransformationExtensions by transformationExtensions {
    override fun Region.highlight(Duration: Duration) {
        exitManager.checkExitRequested()
        platformImpl.highlight(this.transform(), Duration)
    }
}