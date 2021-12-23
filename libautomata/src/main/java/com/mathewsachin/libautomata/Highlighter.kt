package com.mathewsachin.libautomata

import com.mathewsachin.libautomata.extensions.ITransformationExtensions
import javax.inject.Inject
import kotlin.time.Duration

interface Highlighter {
    operator fun invoke(
        region: Region,
        color: HighlightColor,
        duration: Duration = Duration.seconds(0.3)
    )
}

class RealHighlighter @Inject constructor(
    val exitManager: ExitManager,
    val platformImpl: PlatformImpl,
    transformations: ITransformationExtensions
): Highlighter, ITransformationExtensions by transformations {
    override fun invoke(region: Region, color: HighlightColor, duration: Duration) {
        exitManager.checkExitRequested()
        if (platformImpl.prefs.debugMode) {
            platformImpl.highlight(region.transform(), duration, color)
        }
    }
}