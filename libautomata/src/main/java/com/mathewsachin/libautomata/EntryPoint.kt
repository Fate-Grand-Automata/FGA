package com.mathewsachin.libautomata

import kotlin.concurrent.thread

/**
 * Basic class for all "script modes", such as Battle, Lottery and Summoning.
 */
abstract class EntryPoint(
    val exitManager: ExitManager,
    val platformImpl: IPlatformImpl,
    private val messages: IAutomataMessages
) {
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
    fun stop(reason: ScriptAbortException) = exitManager.exit(reason)

    private fun scriptRunner() {
        try {
            script()
        } catch (e: ScriptAbortException) {
            scriptExitListener.invoke(null)

            // Script stopped by user
            if (e.message.isNotBlank()) {
                platformImpl.messageBox(messages.scriptExited, e.message)
            }

            if (e !is ScriptAbortException.User) {
                platformImpl.notify(messages.stoppedByUser)
            }
        } catch (e: ScriptExitException) {
            scriptExitListener.invoke(e)

            // Show the message box only if there is some message
            if (e.message.isNotBlank()) {
                val msg = messages.scriptExited
                platformImpl.messageBox(msg, e.message)
                platformImpl.notify(msg)
            }
        } catch (e: Exception) {
            println(e.messageAndStackTrace)

            scriptExitListener.invoke(e)

            val msg = messages.unexpectedError
            platformImpl.messageBox(msg, e.messageAndStackTrace, e)
            platformImpl.notify(msg)
        } finally {
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
    protected abstract fun script(): Nothing

    /**
     * A listener function, which is called when the script detected an exit condition or when an
     * unexpected error occurred.
     */
    var scriptExitListener: (Exception?) -> Unit = { }
}