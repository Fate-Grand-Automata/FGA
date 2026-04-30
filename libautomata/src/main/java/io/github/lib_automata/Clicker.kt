package io.github.lib_automata

import javax.inject.Inject

interface Clicker {
    operator fun invoke(location: Location, times: Int = 1)
}

class RealClicker @Inject constructor(
    private val gestureService: GestureService,
    private val exitManager: ExitManager,
    private val transform: Transformer,
) : Clicker {
    override fun invoke(location: Location, times: Int) {
        exitManager.checkExitRequested()
        gestureService.click(transform.toScreen(location), times)
    }
}
