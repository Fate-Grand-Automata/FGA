package io.github.fate_grand_automata.scripts.supportSelection

sealed class SupportSelectionResult {
    object Refresh: SupportSelectionResult()
    object ScrollDown: SupportSelectionResult()
    object Done: SupportSelectionResult()
}