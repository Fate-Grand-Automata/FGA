package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.libautomata.Region

abstract class SpecificSupportSelection(
    protected val supportPrefs: ISupportPreferences,
    protected val boundsFinder: SupportBoundsFinder,
    api: IFgoAutomataApi
): SupportSelectionProvider, IFgoAutomataApi by api {
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
                    else -> boundsFinder.findSupportBounds(result.Support)
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
}