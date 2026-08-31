package io.github.lib_automata

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
    fun checkExitRequested() {
        /* Every image search, tap and highlight lands here, so the running-and-not-paused case
           has to skip the dispatch below. Reading both states without locking is enough: this is
           a polling checkpoint, and a stop or pause arriving just after is caught by the next. */
        if (scope.isActive && !pauseMutex.isLocked) {
            return
        }

        runBlockingOnScope {
            pauseMutex.withLock { }
        }
    }

    fun pause() = runBlocking { pauseMutex.lock() }

    fun resume() = runBlocking { pauseMutex.unlock() }

    /**
     * Requests exit
     */
    fun exit() = scope.cancel()
}