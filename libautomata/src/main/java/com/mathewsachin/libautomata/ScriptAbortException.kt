package com.mathewsachin.libautomata

/**
 * An exception for when the user requested the app to stop.
 */
class ScriptAbortException(override val message: String = "") : Exception(message)