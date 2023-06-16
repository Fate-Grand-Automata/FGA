package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.SupportImageKind
import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FriendSupportSelection @Inject constructor(
    supportPrefs: ISupportPreferences,
    boundsFinder: SupportBoundsFinder,
    friendChecker: SupportFriendChecker,
    api: IFgoAutomataApi
): SpecificSupportSelection(supportPrefs, boundsFinder, friendChecker, api) {
    private val friendNames = supportPrefs.friendNames

    override fun search(): SpecificSupportSearchResult {
        if (friendNames.isEmpty()) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionFriendNotSet)
        }

        for (friendName in friendNames) {
            // Cached pattern. Don't dispose here.
            val patterns = images.loadSupportPattern(SupportImageKind.Friend, friendName)

            patterns.forEach { pattern ->
                for (friend in locations.support.friendsRegion.findAll(pattern).sorted()) {
                    return SpecificSupportSearchResult.Found(friend.region)
                }
            }
        }

        return SpecificSupportSearchResult.NotFound
    }
}

