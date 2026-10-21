package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.prefs.ISupportPreferences
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.abs

@ScriptScope
class PreferredSupportSelection @Inject constructor(
    private val supportPrefs: ISupportPreferences,
    api: IFgoAutomataApi,
    private val boundsFinder: SupportBoundsFinder,
    private val friendChecker: SupportFriendChecker,
    private val servantSelection: ServantSelection,
    private val ceSelection: CESelection,
    private val friendSelection: FriendSelection,
    private val colorChecker: SupportColorChecker
) : SupportSelectionProvider, IFgoAutomataApi by api {
    private val servants = supportPrefs.preferredServants
    private val ces = supportPrefs.preferredCEs
    private val friendNames = supportPrefs.friendNames
    private val needsRegularSkills = servants.isNotEmpty() && servantSelection.needsRegularSkillCheck()
    private val needsAppendSkills = servants.isNotEmpty() && servantSelection.needsAppendSkillCheck()

    private enum class SkillDisplay { Regular, Append }
    private var skillDisplay = SkillDisplay.Regular

    override fun onSelectionStart() {
        // FGO always opens a new support screen in the regular-skill display.
        skillDisplay = SkillDisplay.Regular
    }

    private data class ScrollBarState(
        val topScrollbar: Boolean = false,
        val movedScrollbar: Boolean = false,
        val bottomScrollbar: Boolean = false
    ) {
        fun resultWhenNoMatch(): SupportSelectionResult = when {
            topScrollbar -> SupportSelectionResult.ScrollDown
            movedScrollbar && !bottomScrollbar -> SupportSelectionResult.ScrollDown
            else -> SupportSelectionResult.EarlyRefresh
        }
    }

    override fun select(): SupportSelectionResult {
        if (servants.isEmpty() && ces.isEmpty()) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionPreferredNotSet)
        }

        if (useSameSnapIn { supportPrefs.friendsOnly && !friendChecker.isFriend() }) {
            return SupportSelectionResult.Refresh
        }

        val rejectedRows = mutableSetOf<Int>()
        var pendingRow: Int? = null

        while (true) {
            val candidates = findCandidates(rejectedRows)

            if (pendingRow != null) {
                val expectedRow = pendingRow
                val pending = candidates.minByOrNull { abs(it.region.y - expectedRow) }
                    ?.takeIf { abs(it.region.y - expectedRow) <= 20 }

                if (pending != null) {
                    pending.region.click()
                    return SupportSelectionResult.Done
                }

                rejectedRows += expectedRow
                pendingRow = null
                continue
            }

            val candidate = candidates.firstOrNull()
            if (candidate == null) {
                return useSameSnapIn { readScrollBarState().resultWhenNoMatch() }
            }

            val needsOtherDisplay = when (skillDisplay) {
                SkillDisplay.Regular -> needsAppendSkills
                SkillDisplay.Append -> needsRegularSkills
            }

            if (!needsOtherDisplay) {
                candidate.region.click()
                return SupportSelectionResult.Done
            }

            pendingRow = candidate.region.y
            switchSkillDisplay()
        }
    }

    private fun switchSkillDisplay() {
        // The in-game button cycles through three modes:
        // regular only -> append only -> alternating -> regular only.
        // Never inspect the alternating mode because its contents depend on animation timing.
        val clicks = when (skillDisplay) {
            SkillDisplay.Regular -> 1
            SkillDisplay.Append -> 2
        }

        repeat(clicks) {
            locations.support.skillDisplayToggle.click()
            prefs.support.skillDisplayWait.wait()
        }

        skillDisplay = when (skillDisplay) {
            SkillDisplay.Regular -> SkillDisplay.Append
            SkillDisplay.Append -> SkillDisplay.Regular
        }
    }

    private fun findCandidates(rejectedRows: Set<Int>): List<SupportBounds> {
        val grayscaleMatches = useSameSnapIn {
            boundsFinder.all()
                .filterNot { bounds -> rejectedRows.any { abs(it - bounds.region.y) <= 20 } }
                .filter { isInvariantMatch(it) }
                .filter {
                    when (skillDisplay) {
                        SkillDisplay.Regular -> !needsRegularSkills || servantSelection.checkRegularSkills(it)
                        SkillDisplay.Append -> !needsAppendSkills || servantSelection.checkMaxAppendSkills(it)
                    }
                }
        }

        val needsClassScore = servants.isNotEmpty() && colorChecker.needsClassScoreCheck()
        val needsAppendColor = skillDisplay == SkillDisplay.Append &&
            needsAppendSkills && colorChecker.needsColorCheckInAppendMode()

        if (!needsClassScore && !needsAppendColor) return grayscaleMatches

        return useColor {
            useSameSnapIn {
                grayscaleMatches
                    .filter { !needsClassScore || colorChecker.checkClassScore(it) }
                    .filter { !needsAppendColor || colorChecker.checkUnlockedAppendSkills(it) }
            }
        }
    }

    private fun readScrollBarState(): ScrollBarState {
        val topScrollbar = images[Images.SupportScrollBarTop] in locations.support.topScrollbarRegion

        if (topScrollbar) return ScrollBarState(topScrollbar = true)

        return ScrollBarState(
            movedScrollbar = images[Images.SupportScrollBarMoved] in locations.support.topScrollbarRegion,
            bottomScrollbar = images[Images.SupportScrollBarBottom] in locations.support.bottomScrollbarRegion
        )
    }

    private fun isInvariantMatch(bounds: SupportBounds): Boolean {
        if (supportPrefs.friendsOnly && !friendChecker.isFriend(bounds)) {
            return false
        }

        return (
            servantSelection.checkIdentity(servants, bounds) &&
            ceSelection.check(ces, bounds) &&
            (!supportPrefs.friendsOnly || friendSelection.check(friendNames, bounds))
        )
    }
}
