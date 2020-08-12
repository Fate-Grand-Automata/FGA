package com.mathewsachin.libautomata

import kotlinx.coroutines.*
import kotlin.time.Duration

/**
 * Manages the termination of running scripts
 */
class ExitManager {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun wait(duration: Duration) {
        checkExitRequested()

        runBlocking {
            try {
                scope.launch {
                    delay(duration)
                }.join()
            } catch (e: CancellationException) {
                throw ScriptAbortException()
            }
        }
    }

    /**
     * Checks if the stop button has been pressed.
     *
     * @throws ScriptAbortException if the button has been pressed
     */
    fun checkExitRequested() {
        if (!scope.isActive) {
            throw ScriptAbortException()
        }
    }

    /**
     * Requests exit
     */
    fun exit() = scope.cancel()
}