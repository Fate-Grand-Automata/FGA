package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.HighlightColor
import com.mathewsachin.libautomata.PlatformImpl
import com.mathewsachin.libautomata.Region
import javax.inject.Inject
import kotlin.time.Duration

class HighlightExtensions @Inject constructor(
    val exitManager: ExitManager,
    val platformImpl: PlatformImpl,
    transformationExtensions: ITransformationExtensions
) : IHighlightExtensions, ITransformationExtensions by transformationExtensions {
    override fun Region.highlight(color: HighlightColor, duration: Duration) {
        exitManager.checkExitRequested()
        if (platformImpl.prefs.debugMode) {
            platformImpl.highlight(this.transform(), duration, color)
        }
    }
}