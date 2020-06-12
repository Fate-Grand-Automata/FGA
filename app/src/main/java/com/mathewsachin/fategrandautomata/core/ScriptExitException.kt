package com.mathewsachin.fategrandautomata.core

/**
 * An exception for situations where the app itself determined that it cannot continue.
 *
 * That could either be because some error happened or because the user must do something before
 * starting the app again, such as manually selecting a support or refilling AP.
 */
class ScriptExitException(Message: String) : Exception(Message) {

    /**
     * For cases where you want the script to exit but not show the dialog
     */
    constructor() : this("")
}