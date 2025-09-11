package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class PreferredSupportSelection @Inject constructor(
    private val supportPrefs: ISupportPreferences,
    api: IFgoAutomataApi,
    private val boundsFinder: SupportBoundsFinder,
    private val friendChecker: SupportFriendChecker,
    private val servantSelection: ServantSelection,
    private val ceSelection: CESelection,
    private val friendSelection: FriendSelection,
) : SupportSelectionProvider, IFgoAutomataApi by api {
    private val servants = supportPrefs.preferredServants
    private val ces = supportPrefs.preferredCEs
    private val friendNames = supportPrefs.friendNames

    override fun select(): SupportSelectionResult {
        if (servants.isEmpty() && ces.isEmpty()) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionPreferredNotSet)
        }

        return useSameSnapIn {
            if (supportPrefs.friendsOnly && !friendChecker.isFriend()) {
                // no friends on screen, so there's no point in scrolling anymore
                return@useSameSnapIn SupportSelectionResult.Refresh
            }

            val matched = boundsFinder.all()
                .toList()
                .firstNotNullOfOrNull { isMatch(it) }

            if (matched != null) {
                matched.click()
                SupportSelectionResult.Done
            } else {
                var topScrollbar = false
                var movedSrollBar = false
                var bottomScrollbar = false
                useSameSnapIn {
                    topScrollbar = images[Images.SupportScrollBarTop] in locations.support.topScrollbarRegion
                    if (!topScrollbar) {
                        movedSrollBar = images[Images.SupportScrollBarMoved] in locations.support.topScrollbarRegion
                        bottomScrollbar = images[Images.SupportScrollBarBottom] in
                            locations.support.bottomScrollbarRegion
                    }
                }
                when {
                    topScrollbar -> SupportSelectionResult.ScrollDown
                    movedSrollBar && !bottomScrollbar -> SupportSelectionResult.ScrollDown
                    else -> SupportSelectionResult.EarlyRefresh
                }
            }
        }
    }

    private fun isMatch(bounds: SupportBounds): Region? {
        if (supportPrefs.friendsOnly && !friendChecker.isFriend(bounds)) {
            return null
        }

        return if (
            servantSelection.check(servants, bounds) &&
            ceSelection.check(ces, bounds) &&
            (!supportPrefs.friendsOnly || friendSelection.check(friendNames, bounds))
        ) {
            bounds.region
        } else {
            null
        }
    }
}
