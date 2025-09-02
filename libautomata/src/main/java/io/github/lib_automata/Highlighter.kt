package io.github.lib_automata

import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface Highlighter {
    companion object {
        val DEFAULT_DURATION = 0.3.seconds
    }

    operator fun invoke(
        region: Region,
        color: HighlightColor,
        duration: Duration = DEFAULT_DURATION,
        text: String? = null
    )
}

class RealHighlighter @Inject constructor(
    private val exitManager: ExitManager,
    private val platformImpl: PlatformImpl,
    private val transform: Transformer
) : Highlighter {
    override fun invoke(region: Region, color: HighlightColor, duration: Duration, text: String?) {
        exitManager.checkExitRequested()
        if (platformImpl.prefs.debugMode) {
            platformImpl.highlight(transform.toScreen(region), duration, color, text)
        }
    }
}