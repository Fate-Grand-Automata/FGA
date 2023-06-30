package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.SupportSelectionModeEnum
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class SupportFriendChecker @Inject constructor(
    private val supportPrefs: ISupportPreferences,
    api: IFgoAutomataApi
): IFgoAutomataApi by api {
    fun isFriend(region: Region = locations.support.friendRegion): Boolean {
        val onlySelectFriends = supportPrefs.friendsOnly
                || supportPrefs.selectionMode == SupportSelectionModeEnum.Friend

        if (!onlySelectFriends)
            return true

        return sequenceOf(
            images[Images.Friend],
            images[Images.Guest],
            images[Images.Follow]
        ).any { it in region }
    }
}