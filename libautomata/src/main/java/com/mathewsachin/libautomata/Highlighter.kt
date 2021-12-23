package com.mathewsachin.libautomata

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
    private val exitManager: ExitManager,
    private val platformImpl: PlatformImpl,
    private val transform: Transformer
): Highlighter {
    override fun invoke(region: Region, color: HighlightColor, duration: Duration) {
        exitManager.checkExitRequested()
        if (platformImpl.prefs.debugMode) {
            platformImpl.highlight(transform.toScreen(region), duration, color)
        }
    }
}