package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.SupportSelectionModeEnum
import io.github.fate_grand_automata.scripts.supportSelection.FirstSupportSelection
import io.github.fate_grand_automata.scripts.supportSelection.ManualSupportSelection
import io.github.fate_grand_automata.scripts.supportSelection.PreferredSupportSelection
import io.github.fate_grand_automata.scripts.supportSelection.SupportSelectionProvider
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class SupportModeDecider @Inject constructor(
    private val firstSupportSelection: FirstSupportSelection,
    private val preferredSupportSelection: PreferredSupportSelection,
) {
    fun decide(selectionMode: SupportSelectionModeEnum): SupportSelectionProvider =
        when (selectionMode) {
            SupportSelectionModeEnum.First -> firstSupportSelection
            SupportSelectionModeEnum.Manual -> ManualSupportSelection
            SupportSelectionModeEnum.Preferred -> preferredSupportSelection
        }
}