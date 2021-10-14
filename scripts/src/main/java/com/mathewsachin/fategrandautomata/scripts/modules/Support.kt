package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.enums.canAlsoCheckAll
import com.mathewsachin.fategrandautomata.scripts.prefs.ISupportPreferences
import com.mathewsachin.fategrandautomata.scripts.supportSelection.*
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

const val supportRegionToolSimilarity = 0.75

@ScriptScope
class Support @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val firstSupportSelection: FirstSupportSelection,
    private val friendSupportSelection: FriendSupportSelection,
    private val preferredSupportSelection: PreferredSupportSelection,
    private val supportPrefs: ISupportPreferences
) : IFgoAutomataApi by fgAutomataApi {
    private fun selectSupportClass(supportClass: SupportClass = supportPrefs.supportClass) {
        if (supportClass == SupportClass.None)
            return

        game.locate(supportClass).click()

        Duration.seconds(0.5).wait()
    }

    fun selectSupport(continuing: Boolean, selectionMode: SupportSelectionModeEnum = supportPrefs.selectionMode) {
        waitForSupportScreenToLoad()

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
                    refreshSupportList()

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

    private var lastSupportRefreshTimestamp: TimeMark? = null
    private val supportRefreshThreshold = Duration.seconds(10)

    private fun refreshSupportList() {
        lastSupportRefreshTimestamp?.elapsedNow()?.let { elapsed ->
            val toWait = supportRefreshThreshold - elapsed

            if (toWait.isPositive()) {
                messages.notify(ScriptNotify.SupportListUpdatingIn(toWait))

                toWait.wait()
            }
        }

        game.supportUpdateClick.click()
        Duration.seconds(1).wait()

        game.supportUpdateYesClick.click()

        waitForSupportScreenToLoad()
        updateLastSupportRefreshTimestamp()
    }

    private fun updateLastSupportRefreshTimestamp() {
        lastSupportRefreshTimestamp = TimeSource.Monotonic.markNow()
    }

    private fun waitForSupportScreenToLoad() {
        while (true) {
            when {
                needsToRetry() -> retry()
                // wait for dialogs to close
                images[Images.SupportExtra] !in game.supportExtraRegion -> Duration.seconds(1).wait()
                images[Images.SupportNotFound] in game.supportNotFoundRegion -> {
                    updateLastSupportRefreshTimestamp()
                    refreshSupportList()
                    return
                }
                game.supportRegionToolSearchRegion.exists(
                    images[Images.SupportRegionTool],
                    similarity = supportRegionToolSimilarity
                ) -> return
                images[Images.Guest] in game.supportFriendRegion -> return
            }

            Duration.milliseconds(100).wait()
        }
    }
}
