package com.mathewsachin.libautomata

/**
 * An exception for situations where the app itself determined that it cannot continue.
 *
 * That could either be because some error happened or because the user must do something before
 * starting the app again, such as manually selecting a support or refilling AP.
 */
open class ScriptExitException(override val message: String) : Exception(message) {

    /**
     * For cases where you want the script to exit but not show the dialog
     */
    constructor() : this("")
}