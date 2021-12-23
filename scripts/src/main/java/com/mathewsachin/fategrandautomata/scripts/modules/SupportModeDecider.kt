package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.supportSelection.*
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class SupportModeDecider @Inject constructor(
    private val firstSupportSelection: FirstSupportSelection,
    private val friendSupportSelection: FriendSupportSelection,
    private val preferredSupportSelection: PreferredSupportSelection,
) {
    fun decide(selectionMode: SupportSelectionModeEnum): SupportSelectionProvider =
        when (selectionMode) {
            SupportSelectionModeEnum.First -> firstSupportSelection
            SupportSelectionModeEnum.Manual -> ManualSupportSelection
            SupportSelectionModeEnum.Friend -> friendSupportSelection
            SupportSelectionModeEnum.Preferred -> preferredSupportSelection
        }
}