package com.mathewsachin.libautomata

/**
 * Manages the termination of running scripts
 */
class ExitManager {
    @Volatile
    private var exitRequested = false

    /**
     * Checks if the stop button has been pressed.
     *
     * @throws ScriptAbortException if the button has been pressed
     */
    fun checkExitRequested() {
        if (exitRequested) {
            throw ScriptAbortException()
        }
    }

    /**
     * Requests exit
     */
    fun request() {
        exitRequested = true
    }

    /**
     * Cancels exit request
     */
    fun cancel() {
        exitRequested = false
    }
}