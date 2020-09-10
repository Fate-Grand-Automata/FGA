package com.mathewsachin.libautomata

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

/**
 * Manages the termination of running scripts
 */
class ExitManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val pauseMutex = Mutex()

    fun wait(duration: Duration) {
        checkExitRequested()

        runBlockingOnScope {
            delay(duration)
        }
    }

    private fun runBlockingOnScope(block: suspend () -> Unit) = runBlocking {
        try {
            withContext(scope.coroutineContext) {
                block()
            }
        } catch (e: CancellationException) {
            throw ScriptAbortException()
        }
    }

    /**
     * Checks if the stop button has been pressed.
     *
     * @throws ScriptAbortException if the button has been pressed
     */
    fun checkExitRequested() = runBlockingOnScope {
        pauseMutex.withLock { }
    }

    fun pause() = runBlocking { pauseMutex.lock() }

    fun resume() = runBlocking { pauseMutex.unlock() }

    /**
     * Requests exit
     */
    fun exit() = scope.cancel()
}