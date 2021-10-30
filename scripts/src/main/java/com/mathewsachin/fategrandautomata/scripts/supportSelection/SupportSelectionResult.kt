package com.mathewsachin.fategrandautomata.scripts.supportSelection

sealed class SupportSelectionResult {
    object Refresh: SupportSelectionResult()
    object ScrollDown: SupportSelectionResult()
    object Done: SupportSelectionResult()
}