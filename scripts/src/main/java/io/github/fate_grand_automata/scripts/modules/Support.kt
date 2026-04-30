package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.SupportSelectionModeEnum
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.fate_grand_automata.scripts.supportSelection.ManualSupportSelection
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class Support @Inject constructor(
    private val decider: SupportModeDecider,
    private val loop: SupportSelectionLoop,
    private val supportPrefs: ISupportPreferences,
) {
    companion object {
        const val SUPPORT_REGION_TOOL_SIMILARITY = 0.75
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
