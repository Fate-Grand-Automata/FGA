package com.mathewsachin.libautomata

import javax.inject.Inject
import kotlin.time.Duration

interface Waiter {
    operator fun invoke(duration: Duration)
}

class RealWaiter @Inject constructor(
    private val platformImpl: PlatformImpl,
    private val exitManager: ExitManager
): Waiter {
    override fun invoke(duration: Duration) {
        val multiplier = platformImpl.prefs.waitMultiplier
        exitManager.wait(duration * multiplier)
    }
}