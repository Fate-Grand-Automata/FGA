package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class FriendSupportSelection @Inject constructor(
    supportPrefs: ISupportPreferences,
    fgAutomataApi: IFgoAutomataApi
): SpecificSupportSelection(supportPrefs, fgAutomataApi) {
    private val friendNames = supportPrefs.friendNames

    override fun search(): SpecificSupportSearchResult {
        if (friendNames.isEmpty()) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionFriendNotSet)
        }

        for (friendName in friendNames) {
            // Cached pattern. Don't dispose here.
            val patterns = images.loadSupportPattern(SupportImageKind.Friend, friendName)

            patterns.forEach { pattern ->
                for (friend in game.support.friendsRegion.findAll(pattern).sorted()) {
                    return SpecificSupportSearchResult.Found(friend.region)
                }
            }
        }

        return SpecificSupportSearchResult.NotFound
    }
}

