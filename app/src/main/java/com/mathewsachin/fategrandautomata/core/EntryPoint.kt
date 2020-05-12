package com.mathewsachin.fategrandautomata.core

import kotlin.Exception
import kotlin.concurrent.thread

abstract class EntryPoint {
    fun run() {
        thread(start = true) {
            scriptRunner()
        }
    }

    fun stop() {
        AutomataApi.exitRequested = true
    }

    private fun scriptRunner() {
        try {
            script()
        }
        catch (e: ScriptAbortException) {
            // Script stopped by user
        }
        catch (e: ScriptExitException) {
            scriptExitListener?.invoke()

            AutomataApi.showMessageBox("Script Exited", e.message ?: "")
        }
        catch (e: Exception) {
            println(e.toString())

            scriptExitListener?.invoke()

            AutomataApi.showMessageBox("Unexpected Error", e.toString())
        }
    }

    protected abstract fun script()

    var scriptExitListener: (() -> Unit)? = null
}