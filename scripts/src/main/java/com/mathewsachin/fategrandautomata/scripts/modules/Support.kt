package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.enums.canAlsoCheckAll
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.fategrandautomata.scripts.supportSelection.*
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class Support @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val firstSupportSelection: FirstSupportSelection,
    private val friendSupportSelection: FriendSupportSelection,
    private val preferredSupportSelection: PreferredSupportSelection,
    private val supportPrefs: ISupportPreferences,
    private val refresher: SupportScreenRefresher
) : IFgoAutomataApi by fgAutomataApi {
    companion object {
        const val supportRegionToolSimilarity = 0.75
    }

    private fun selectSupportClass(supportClass: SupportClass = supportPrefs.supportClass) {
        if (supportClass == SupportClass.None)
            return

        game.locate(supportClass).click()

        Duration.seconds(0.5).wait()
    }

    fun selectSupport(continuing: Boolean, selectionMode: SupportSelectionModeEnum = supportPrefs.selectionMode) {
        refresher.waitForSupportScreenToLoad()

        val provider = when (selectionMode) {
            SupportSelectionModeEnum.First -> firstSupportSelection
            SupportSelectionModeEnum.Manual -> ManualSupportSelection
            SupportSelectionModeEnum.Friend -> friendSupportSelection
            SupportSelectionModeEnum.Preferred -> preferredSupportSelection
        }

        execute(provider, continuing)
    }

    private fun execute(provider: SupportSelectionProvider, continuing: Boolean) {
        var numberOfSwipes = 0
        var numberOfUpdates = 0
        var onAllList = false

        val alsoCheckAll = supportPrefs.alsoCheckAll && supportPrefs.supportClass.canAlsoCheckAll
        if (alsoCheckAll || !continuing) {
            selectSupportClass()
        }

        while (true) {
            val result = provider.select()

            when {
                result is SupportSelectionResult.Done -> return
                // Scroll down as long as we don't exceed max swipes
                result is SupportSelectionResult.ScrollDown
                        && numberOfSwipes < prefs.support.swipesPerUpdate -> {

                    swipe(
                        game.supportListSwipeStart,
                        game.supportListSwipeEnd
                    )

                    ++numberOfSwipes
                    Duration.seconds(0.3).wait()
                }
                // Switch to All if user asked to
                alsoCheckAll && !onAllList -> {
                    selectSupportClass(SupportClass.All)
                    onAllList = true
                    numberOfSwipes = 0
                }
                // Refresh support list if not exceeded max refreshes
                numberOfUpdates < prefs.support.maxUpdates -> {
                    refresher.refreshSupportList()

                    if (onAllList) {
                        Duration.seconds(0.5).wait()
                        selectSupportClass()
                        onAllList = false
                    }

                    ++numberOfUpdates
                    numberOfSwipes = 0
                }
                // Not found after retries, use fallback
                else -> {
                    game.supportListTopClick.click()
                    selectSupport(true, supportPrefs.fallbackTo)
                    return
                }
            }
        }
    }
}
