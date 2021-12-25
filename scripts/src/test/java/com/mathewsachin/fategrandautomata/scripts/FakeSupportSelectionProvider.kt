package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.supportSelection.SupportSelectionProvider
import com.mathewsachin.fategrandautomata.scripts.supportSelection.SupportSelectionResult

class FakeSupportSelectionProvider(
    private val func: (Int) -> SupportSelectionResult
) : SupportSelectionProvider {
    private var index = 0

    override fun select() =
        func(index++)
}