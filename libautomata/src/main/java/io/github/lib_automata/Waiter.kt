package io.github.lib_automata

import javax.inject.Inject
import kotlin.time.Duration

interface Waiter {
    operator fun invoke(duration: Duration, applyMultiplier: Boolean = true)
}

class RealWaiter @Inject constructor(
    private val platformImpl: PlatformImpl,
    private val exitManager: ExitManager,
) : Waiter {
    override fun invoke(duration: Duration, applyMultiplier: Boolean) {
        val multiplier = if (applyMultiplier) platformImpl.prefs.waitMultiplier else 1.0
        exitManager.wait(duration * multiplier)
    }
}
