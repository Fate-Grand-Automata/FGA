package io.github.lib_automata

import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface LongPress {
    operator fun invoke(location: Location, duration: Duration = 2.seconds)
}

class RealLongPress @Inject constructor(
    private val gestureService: GestureService,
    private val exitManager: ExitManager,
    private val transform: Transformer
) : LongPress {
    override fun invoke(location: Location, duration: Duration) {
        exitManager.checkExitRequested()
        gestureService.longPress(transform.toScreen(location), duration)
    }
}