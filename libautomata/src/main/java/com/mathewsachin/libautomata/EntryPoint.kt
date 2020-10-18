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
    fun stop() = exitManager.exit()

    private fun scriptRunner() {
        try {
            script()
        } catch (e: ScriptAbortException) {
            if (e.message.isNotBlank()) {
                platformImpl.messageBox(messages.scriptExited, e.message) {
                    onExit()
                }
            } else onExit()
        } catch (e: ScriptExitException) {
            // Show the message box only if there is some message
            if (e.message.isNotBlank()) {
                val msg = messages.scriptExited
                platformImpl.notify(msg)

                platformImpl.messageBox(msg, e.message) {
                    onExit(e)
                }
            } else onExit(e)
        } catch (e: Exception) {
            println(e.messageAndStackTrace)

            val msg = messages.unexpectedError
            platformImpl.notify(msg)

            platformImpl.messageBox(msg, e.messageAndStackTrace, e) {
                onExit(e)
            }
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

    private fun onExit(e: Exception? = null) {
        scriptExitListener.invoke(e)

        scriptExitListener = { }
    }

    /**
     * A listener function, which is called when the script detected an exit condition or when an
     * unexpected error occurred.
     */
    var scriptExitListener: (Exception?) -> Unit = { }
}