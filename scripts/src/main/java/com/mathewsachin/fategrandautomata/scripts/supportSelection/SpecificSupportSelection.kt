package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptLog
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.modules.Support
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.Region

abstract class SpecificSupportSelection(
    protected val supportPrefs: ISupportPreferences,
    fgAutomataApi: IFgoAutomataApi
): SupportSelectionProvider, IFgoAutomataApi by fgAutomataApi {
    protected abstract fun search(): SpecificSupportSearchResult

    override fun select() =
        useSameSnapIn(fun(): SupportSelectionResult {
            if (!isFriend(locations.support.friendRegion)) {
                // no friends on screen, so there's no point in scrolling anymore
                return SupportSelectionResult.Refresh
            }

            val result = search()

            if (result is SpecificSupportSearchResult.Found) {
                val bounds = when (result) {
                    is SpecificSupportSearchResult.FoundWithBounds -> result.Bounds
                    // bounds are not returned by all methods
                    else -> findSupportBounds(result.Support)
                }

                if (!isFriend(bounds)) {
                    // found something, but it doesn't belong to a friend. keep scrolling
                    return SupportSelectionResult.ScrollDown
                }

                result.Support.click()
                return SupportSelectionResult.Done
            }

            // nope, not found this time. keep scrolling
            return SupportSelectionResult.ScrollDown
        })

    private fun isFriend(region: Region): Boolean {
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

    protected fun findSupportBounds(support: Region) =
        locations.support.confirmSetupButtonRegion
            .findAll(
                images[Images.SupportConfirmSetupButton],
                Support.supportRegionToolSimilarity
            )
            .map {
                locations.support.defaultBounds
                    .copy(y = it.region.y - 70)
            }
            .firstOrNull { support in it }
            ?: locations.support.defaultBounds.also {
                messages.log(ScriptLog.DefaultSupportBounds)
            }
}