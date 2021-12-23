package com.mathewsachin.libautomata.extensions

import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.PlatformImpl
import javax.inject.Inject
import kotlin.time.Duration

class DurationExtensions @Inject constructor(
    val platformImpl: PlatformImpl,
    val exitManager: ExitManager
) : IDurationExtensions {
    override fun Duration.wait() {
        val multiplier = platformImpl.prefs.waitMultiplier
        exitManager.wait(this * multiplier)
    }
}