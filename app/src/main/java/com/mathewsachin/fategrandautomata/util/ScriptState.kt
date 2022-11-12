package com.mathewsachin.fategrandautomata.util

import com.mathewsachin.libautomata.EntryPoint

sealed class ScriptState {
    object Stopped : ScriptState()
    class Started(
        val entryPoint: EntryPoint,
        var paused: Boolean = false
    ) : ScriptState()

    class Stopping(val start: Started) : ScriptState()
}