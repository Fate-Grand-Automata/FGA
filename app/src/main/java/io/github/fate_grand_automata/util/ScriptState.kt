package io.github.fate_grand_automata.util

import io.github.lib_automata.EntryPoint

sealed class ScriptState {
    object Stopped : ScriptState()
    class Started(
        val entryPoint: EntryPoint,
        val recording: AutoCloseable?,
        var paused: Boolean = false,
    ) : ScriptState()

    class Stopping(val start: Started) : ScriptState()
}
