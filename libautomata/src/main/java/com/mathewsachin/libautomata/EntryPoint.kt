package com.mathewsachin.libautomata

import kotlin.concurrent.thread

/**
 * Basic class for all "script modes", such as Battle, Lottery and Summoning.
 */
abstract class EntryPoint(
    val exitManager: ExitManager,
    val platformImpl: IPlatformImpl
) {
    /**
     * Starts the logic of the script mode in a new thread.
     */
    fun run() {
        exitManager.cancel()

        thread(start = true) {
            scriptRunner()
        }
    }

    /**
     * Notifies the script that the user requested it to stop.
     */
    fun stop() = exitManager.request()

    private fun scriptRunner() {
        try {
            script()
        } catch (e: ScriptAbortException) {
            // Script stopped by user
        } catch (e: ScriptExitException) {
            scriptExitListener.invoke(e)

            // Show the message box only if there is some message
            if (!e.message.isBlank()) {
                platformImpl.messageBox("Script Exited", e.message)
            }
        } catch (e: Exception) {
            println(e.messageAndStackTrace)

            scriptExitListener.invoke(e)

            platformImpl.messageBox("Unexpected Error", e.messageAndStackTrace, e)
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
    protected abstract fun script(): Nothing

    /**
     * A listener function, which is called when the script detected an exit condition or when an
     * unexpected error occurred.
     */
    var scriptExitListener: (Exception?) -> Unit = { }
}