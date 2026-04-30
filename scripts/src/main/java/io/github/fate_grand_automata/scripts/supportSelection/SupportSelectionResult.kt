package io.github.fate_grand_automata.scripts.supportSelection

sealed class SupportSelectionResult {
    data object Refresh : SupportSelectionResult()
    data object ScrollDown : SupportSelectionResult()
    data object EarlyRefresh : SupportSelectionResult()
    data object Done : SupportSelectionResult()
}
