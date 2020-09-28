package com.mathewsachin.libautomata

/**
 * An exception for when the user requested the app to stop.
 */
sealed class ScriptAbortException(override val message: String) : Exception(message) {
    class User(message: String = "") : ScriptAbortException(message)
    class ScreenTurnedOff(message: String = "") : ScriptAbortException(message)
}