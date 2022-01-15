package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.SupportImageKind
import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@ScriptScope
class CESelection @Inject constructor(
    api: IFgoAutomataApi,
    private val supportPrefs: ISupportPreferences,
    private val starChecker: SupportSelectionStarChecker
): IFgoAutomataApi by api {
    class FoundCE(val mlb: Boolean)

    suspend fun check(ces: List<String>, bounds: SupportBounds): FoundCE? {
        // TODO: Only check the lower part (excluding Servant)
        val searchRegion = bounds.region intersect locations.support.listRegion ?: return null

        val mlb = isLimitBroken(searchRegion)

        if (ces.isEmpty())
            return FoundCE(mlb)

        if (supportPrefs.mlb && !mlb)
            return null

        val matched = coroutineScope {
            ces
                .flatMap { entry -> images.loadSupportPattern(SupportImageKind.CE, entry) }
                .map {
                    async { it in searchRegion }
                }
                .any { it.await() }
        }

        return FoundCE(mlb).takeIf { matched }
    }

    private fun isLimitBroken(craftEssence: Region): Boolean {
        val limitBreakRegion = locations.support.limitBreakRegion
            .copy(y = craftEssence.y)

        return starChecker.isStarPresent(limitBreakRegion)
    }
}