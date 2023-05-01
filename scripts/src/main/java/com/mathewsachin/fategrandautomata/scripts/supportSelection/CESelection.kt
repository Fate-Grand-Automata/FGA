package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import java.util.stream.Collectors
import javax.inject.Inject
import kotlin.streams.asStream

@ScriptScope
class CESelection @Inject constructor(
    api: IFgoAutomataApi,
    private val supportPrefs: ISupportPreferences,
    private val starChecker: SupportSelectionStarChecker
) : IFgoAutomataApi by api {
    data class FoundCE(val region: Region, val mlb: Boolean) : Comparable<FoundCE> {
        override fun compareTo(other: FoundCE) = when {
            // Prefer MLB
            mlb && !other.mlb -> -1
            !mlb && other.mlb -> 1
            else -> region.compareTo(other.region)
        }
    }

    fun findCraftEssences(ces: List<String>, searchRegion: Region): List<FoundCE> =
        ces
            .flatMap { entry -> images.loadSupportPattern(SupportImageKind.CE, entry) }
            .parallelStream()
            .flatMap { pattern ->
                searchRegion
                    .findAll(pattern)
                    .asStream()
                    .map { FoundCE(it.region, isLimitBroken(it.region)) }
                    .filter { !supportPrefs.mlb || it.mlb }
            }
            .sorted()
            .collect(Collectors.toList())

    private fun isLimitBroken(craftEssence: Region): Boolean {
        val limitBreakRegion = locations.support.limitBreakRegion
            .copy(y = craftEssence.y)

        return starChecker.isStarPresent(limitBreakRegion)
    }
}