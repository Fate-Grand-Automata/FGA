package com.mathewsachin.libautomata

import kotlin.concurrent.thread

/**
 * Basic class for all "script modes", such as Battle, Lottery and Summoning.
 */
abstract class EntryPoint(val exitManager: ExitManager) {
    /**
     * Starts the logic of the script mode in a new thread.
     */
    fun run() {
        thread(start = true) {
            scriptRunner()
        }
    }

    /**
     * Notifies the script that the user requested it to stop.
     */
    fun stop() = exitManager.exit()

    private fun scriptRunner() {
        try {
            script()
        } catch (e: Exception) {
            scriptExitListener.invoke(e)

            scriptExitListener = { }
        }
    }

    /**
     * Method containing the main logic of the script.
     *
     * Normally, the main logic runs in a loop until either the user stops the script or an exit
     * condition is reached.
     *
     * @throws ScriptAbortException when the user stopped the script
     * @throws ScriptExitException when an exit condition was reached
     */
    abstract fun script(): Nothing

    /**
     * A listener function, which is called when the script detected an exit condition or when an
     * unexpected error occurred.
     */
    var scriptExitListener: (Exception) -> Unit = { }
}