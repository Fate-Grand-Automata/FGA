package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.Location
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class PreferredSupportSelection @Inject constructor(
    supportPrefs: ISupportPreferences,
    api: IFgoAutomataApi,
    boundsFinder: SupportBoundsFinder,
    friendChecker: SupportFriendChecker,
    private val servantSelection: ServantSelection,
    private val ceSelection: CESelection
): SpecificSupportSelection(supportPrefs, boundsFinder, friendChecker, api) {
    private val servants = supportPrefs.preferredServants
    private val ces = supportPrefs.preferredCEs

    private enum class Mode {
        Servants, CEs, Both, None
    }

    private fun detectMode(): Mode {
        val hasServants = servants.isNotEmpty()
        val hasCEs = ces.isNotEmpty()

        return when {
            hasServants && hasCEs -> Mode.Both
            hasServants -> Mode.Servants
            hasCEs -> Mode.CEs
            else -> Mode.None
        }
    }

    override fun search() = when (detectMode()) {
        Mode.Servants -> servantSelection.findServants(servants).firstOrNull() ?: SpecificSupportSearchResult.NotFound
        Mode.CEs -> {
            ceSelection.findCraftEssences(ces, locations.support.listRegion)
                .map { SpecificSupportSearchResult.Found(it.region) }
                .firstOrNull() ?: SpecificSupportSearchResult.NotFound
        }
        Mode.Both -> searchServantAndCE()
        Mode.None -> throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionPreferredNotSet)
    }

    private fun searchServantAndCE(): SpecificSupportSearchResult =
        servantSelection.findServants(servants)
            .mapNotNull {
                val supportBounds = when (it) {
                    is SpecificSupportSearchResult.FoundWithBounds -> it.Bounds
                    else -> boundsFinder.findSupportBounds(it.Support)
                }

                val ceBounds = locations.support.defaultCeBounds + Location(0, supportBounds.y)
                ceSelection.findCraftEssences(ces, ceBounds).firstOrNull()
                    ?.let { ce -> FoundServantAndCE(supportBounds, ce) }
            }
            .sortedBy { it.ce }
            .map { SpecificSupportSearchResult.FoundWithBounds(it.ce.region, it.supportBounds) }
            .firstOrNull() ?: SpecificSupportSearchResult.NotFound

    private data class FoundServantAndCE(val supportBounds: Region, val ce: CESelection.FoundCE)
}