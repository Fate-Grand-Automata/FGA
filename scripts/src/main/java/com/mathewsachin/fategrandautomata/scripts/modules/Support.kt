package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.fategrandautomata.scripts.supportSelection.ManualSupportSelection
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class Support @Inject constructor(
    private val decider: SupportModeDecider,
    private val loop: SupportSelectionLoop,
    private val supportPrefs: ISupportPreferences
) {
    companion object {
        const val supportRegionToolSimilarity = 0.75
    }

    fun selectSupport(selectionMode: SupportSelectionModeEnum = supportPrefs.selectionMode) {
        val provider = decider.decide(selectionMode)

        if (!loop.select(provider)) {
            val fallback = decider.decide(supportPrefs.fallbackTo)

            if (!loop.select(fallback)) {
                // give up
                ManualSupportSelection.select()
            }
        }
    }
}
