package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences

abstract class SpecificSupportSelection(
    protected val supportPrefs: ISupportPreferences,
    protected val boundsFinder: SupportBoundsFinder,
    private val friendChecker: SupportFriendChecker,
    api: IFgoAutomataApi
): SupportSelectionProvider, IFgoAutomataApi by api {
    protected abstract fun search(): SpecificSupportSearchResult

    override fun select() =
        useSameSnapIn(fun(): SupportSelectionResult {
            if (!friendChecker.isFriend()) {
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

                if (!friendChecker.isFriend(bounds)) {
                    // found something, but it doesn't belong to a friend. keep scrolling
                    return SupportSelectionResult.ScrollDown
                }

                result.Support.click()
                return SupportSelectionResult.Done
            }

            // nope, not found this time. keep scrolling
            return SupportSelectionResult.ScrollDown
        })
}