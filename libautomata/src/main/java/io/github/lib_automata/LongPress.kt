package io.github.lib_automata

import javax.inject.Inject

interface LongPress {
    operator fun invoke(location: Location, duration: Int = 2000)
}

class RealLongPress @Inject constructor(
    private val gestureService: GestureService,
    private val exitManager: ExitManager,
    private val transform: Transformer
) : LongPress {
    override fun invoke(location: Location, duration: Int) {
        exitManager.checkExitRequested()
        gestureService.longPress(transform.toScreen(location), duration)
    }
}