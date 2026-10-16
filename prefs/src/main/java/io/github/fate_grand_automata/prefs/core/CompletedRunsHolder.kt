package io.github.fate_grand_automata.prefs.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks the number of battles completed by the currently running script.
 *
 * This is in-memory only: the count is only meaningful while a script is running and
 * is reset the next time one is launched, so persisting it as a preference would be
 * pointless disk I/O.
 */
@Singleton
class CompletedRunsHolder @Inject constructor() {
    private val _completedRuns = MutableStateFlow(0)
    val completedRuns: StateFlow<Int> = _completedRuns

    fun update(runs: Int) {
        _completedRuns.value = runs
    }

    fun reset() {
        _completedRuns.value = 0
    }
}
