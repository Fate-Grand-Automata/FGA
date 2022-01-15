package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ScriptScope
class PreferredSupportSelection @Inject constructor(
    private val supportPrefs: ISupportPreferences,
    api: IFgoAutomataApi,
    private val boundsFinder: SupportBoundsFinder,
    private val friendChecker: SupportFriendChecker,
    private val servantSelection: ServantSelection,
    private val ceSelection: CESelection,
    private val friendSelection: FriendSelection
): SupportSelectionProvider, IFgoAutomataApi by api {
    private val servants = supportPrefs.preferredServants
    private val ces = supportPrefs.preferredCEs
    private val friendNames = supportPrefs.friendNames

    override fun select(): SupportSelectionResult {
        if (servants.isEmpty() && ces.isEmpty() && friendNames.isEmpty()) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionPreferredNotSet)
        }

        if (supportPrefs.requireFriends && !friendChecker.isFriend()) {
            // no friends on screen, so there's no point in scrolling anymore
            return SupportSelectionResult.Refresh
        }

        val matched = runBlocking {
            boundsFinder.all()
                .toList()
                .map { async { isMatch(it) } }
                .awaitAll()
                .filterNotNull()
                .minOrNull()
        }

        return if (matched != null) {
            matched.region.click()
            SupportSelectionResult.Done
        } else {
            // nope, not found this time. keep scrolling
            SupportSelectionResult.ScrollDown
        }
    }

    private class SupportMatched(
        val region: Region,
        val mlb: Boolean,
        val maxedSkills: List<Boolean>,
        val maxAscended: Boolean
    ): Comparable<SupportMatched> {
        override fun compareTo(other: SupportMatched): Int {
            val myMaxSkillCount = maxedSkills.count { it }
            val otherMaxSkillCount = other.maxedSkills.count { it }

            return when {
                // Prefer MLB
                mlb && !other.mlb -> -1
                !mlb && other.mlb -> 1

                // Prefer more maxed skills
                myMaxSkillCount > otherMaxSkillCount -> -1
                otherMaxSkillCount > myMaxSkillCount -> 1

                // Prefer max ascended
                maxAscended && !other.maxAscended -> -1
                !maxAscended && other.maxAscended -> 1

                // Prefer top-to-bottom
                else -> region.compareTo(other.region)
            }
        }
    }

    private suspend fun isMatch(bounds: SupportBounds): SupportMatched? {
        if (supportPrefs.requireFriends && !friendChecker.isFriend(bounds)) {
            return null
        }

        return coroutineScope {
            val servantCheck = async { servantSelection.check(servants, bounds) }
            val ceCheck = async { ceSelection.check(ces, bounds) }
            val friendPassed = async { !supportPrefs.requireFriends || friendSelection.check(friendNames, bounds) }

            val ceCheckResult = ceCheck.await()
            val servantCheckResult = servantCheck.await()

            if (ceCheckResult != null && servantCheckResult != null && friendPassed.await())
                SupportMatched(
                    region = bounds.region,
                    mlb = ceCheckResult.mlb,
                    maxedSkills = servantCheckResult.maxedSkills,
                    maxAscended = servantCheckResult.maxAscended
                )
            else null
        }
    }
}