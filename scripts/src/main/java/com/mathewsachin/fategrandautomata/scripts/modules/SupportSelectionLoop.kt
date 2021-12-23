package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferencesCommon
import com.mathewsachin.fategrandautomata.scripts.supportSelection.SupportSelectionProvider
import com.mathewsachin.fategrandautomata.scripts.supportSelection.SupportSelectionResult
import javax.inject.Inject
import kotlin.time.Duration

class SupportSelectionLoop @Inject constructor(
    private val screen: SupportScreen,
    private val commonSupportPrefs: ISupportPreferencesCommon,
    private val refresher: SupportScreenRefresher,
    private val supportClassPicker: SupportClassPicker
) {
    fun select(provider: SupportSelectionProvider): Boolean {
        var numberOfSwipes = 0
        var numberOfUpdates = 0
        var onAllList = false
        val alsoCheckAll = supportClassPicker.shouldAlsoCheckAll()
        refresher.waitForSupportScreenToLoad()

        while (true) {
            val result = if (screen.noSupportsPresent()) SupportSelectionResult.Refresh else provider.select()

            when {
                result is SupportSelectionResult.Done -> return true
                // Scroll down as long as we don't exceed max swipes
                result is SupportSelectionResult.ScrollDown
                        && numberOfSwipes < commonSupportPrefs.swipesPerUpdate -> {

                    screen.scrollDown()

                    ++numberOfSwipes
                    screen.delay(Duration.seconds(0.3))
                }
                // Switch to All if user asked to
                alsoCheckAll && !onAllList -> {
                    supportClassPicker.selectSupportClass(SupportClass.All)
                    onAllList = true
                    numberOfSwipes = 0
                }
                // Refresh support list if not exceeded max refreshes
                numberOfUpdates < commonSupportPrefs.maxUpdates -> {
                    refresher.refreshSupportList()
                    onAllList = false

                    ++numberOfUpdates
                    numberOfSwipes = 0
                }
                // Not found after retries, use fallback
                else -> {
                    screen.scrollToTop()
                    return false
                }
            }
        }
    }
}