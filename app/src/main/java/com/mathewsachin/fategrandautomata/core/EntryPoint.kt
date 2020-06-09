package com.mathewsachin.fategrandautomata.core

import java.io.PrintWriter
import java.io.StringWriter
import kotlin.concurrent.thread

val Exception.info get (): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    printStackTrace(pw)
    return "${message}\n\n${sw}"
}

/**
 * Basic class for all "script modes", such as Battle, Lottery and Summoning.
 */
abstract class EntryPoint {
    /**
     * Starts the logic of the script mode in a new thread.
     */
    fun run() {
        ExitManager.cancel()

        thread(start = true) {
            scriptRunner()
        }
    }

    /**
     * Notifies the script that the user requested it to stop.
     */
    fun stop() = ExitManager.request()

    private fun scriptRunner() {
        try {
            script()
        } catch (e: ScriptAbortException) {
            // Script stopped by user
        } catch (e: ScriptExitException) {
            scriptExitListener?.invoke()

            if (!e.message.isNullOrBlank()) {
                AutomataApi.PlatformImpl.messageBox("Script Exited", e.message)
            }
        } catch (e: Exception) {
             println(e.info)

            scriptExitListener?.invoke()

            AutomataApi.PlatformImpl.messageBox("Unexpected Error", e.info, e)
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
    var scriptExitListener: (() -> Unit)? = null
}