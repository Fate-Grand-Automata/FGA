package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.supportSelection.FirstSupportSelection
import com.mathewsachin.fategrandautomata.scripts.supportSelection.ManualSupportSelection
import com.mathewsachin.fategrandautomata.scripts.supportSelection.PreferredSupportSelection
import com.mathewsachin.fategrandautomata.scripts.supportSelection.SupportSelectionProvider
import com.mathewsachin.libautomata.dagger.ScriptScope
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