package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.fategrandautomata.scripts.enums.SupportClass
import com.mathewsachin.fategrandautomata.scripts.enums.SupportSelectionModeEnum
import com.mathewsachin.fategrandautomata.scripts.supportSelection.*
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

const val supportRegionToolSimilarity = 0.75

class Support(
    fgAutomataApi: IFgoAutomataApi
) : IFgoAutomataApi by fgAutomataApi {
    private var servants = listOf<String>()
    private var friendNames = listOf<String>()
    private var ces = listOf<String>()
    private val supportPrefs get() = prefs.selectedBattleConfig.support

    private val firstSupportSelection = FirstSupportSelection(this)
    private val friendSupportSelection by lazy {
        FriendSupportSelection(
            friendNames = friendNames,
            supportPrefs = supportPrefs,
            fgAutomataApi = this
        )
    }
    private val preferredSupportSelection by lazy {
        PreferredSupportSelection(
            servants = servants,
            ces = ces,
            supportPrefs = supportPrefs,
            fgAutomataApi = this
        )
    }

    fun init() {
        friendNames = supportPrefs.friendNames
        servants = supportPrefs.preferredServants
        ces = supportPrefs.preferredCEs
    }

    private fun selectSupportClass(supportClass: SupportClass = supportPrefs.supportClass) {
        if (supportClass == SupportClass.None)
            return

        game.locate(supportClass).click()

        Duration.seconds(0.5).wait()
    }

    fun selectSupport(selectionMode: SupportSelectionModeEnum, continuing: Boolean) {
        waitForSupportScreenToLoad()

        if (!continuing) {
            selectSupportClass()
        }

        val provider = when (selectionMode) {
            SupportSelectionModeEnum.First -> firstSupportSelection
            SupportSelectionModeEnum.Manual -> ManualSupportSelection
            SupportSelectionModeEnum.Friend -> friendSupportSelection
            SupportSelectionModeEnum.Preferred -> preferredSupportSelection
        }

        execute(provider)
    }

    private fun execute(provider: SupportSelectionProvider) {
        var numberOfSwipes = 0
        var numberOfUpdates = 0

        val alsoCheckAll = supportPrefs.alsoCheckAll
                && supportPrefs.supportClass !in listOf(SupportClass.None, SupportClass.All, SupportClass.Mix)

        while (true) {
            val result = provider.select()

            when {
                result is SupportSelectionResult.Done -> return
                result is SupportSelectionResult.ScrollDown
                        && numberOfSwipes < prefs.support.swipesPerUpdate -> {

                    swipe(
                        game.supportListSwipeStart,
                        game.supportListSwipeEnd
                    )

                    ++numberOfSwipes
                    Duration.seconds(0.3).wait()
                }
                numberOfUpdates < prefs.support.maxUpdates -> {
                    refreshSupportList()

                    ++numberOfUpdates
                    numberOfSwipes = 0
                }
                else -> {
                    // -- okay, we have run out of options, let's give up
                    game.supportListTopClick.click()
                    selectSupport(supportPrefs.fallbackTo, true)
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
