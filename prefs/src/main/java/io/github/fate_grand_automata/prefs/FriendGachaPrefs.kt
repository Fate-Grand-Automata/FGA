package io.github.fate_grand_automata.prefs

import io.github.fate_grand_automata.prefs.core.FriendGachaPrefsCore
import io.github.fate_grand_automata.scripts.prefs.IFriendGachaPreferences

internal class FriendGachaPrefs(
    val prefs: FriendGachaPrefsCore
) : IFriendGachaPreferences {

    override var shouldLimitFP by prefs.shouldLimitFP
    override var limitFP by prefs.limitFP
    override var shouldCreateCEBombAfterSummon by prefs.shouldCreateCEBombAfterSummon

}