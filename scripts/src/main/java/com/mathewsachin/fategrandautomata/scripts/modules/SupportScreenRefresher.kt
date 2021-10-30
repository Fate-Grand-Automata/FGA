package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@ScriptScope
class SupportScreenRefresher @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val connectionRetry: ConnectionRetry
) : IFgoAutomataApi by fgAutomataApi {
    private var lastSupportRefreshTimestamp: TimeMark? = null
    private val supportRefreshThreshold = Duration.seconds(10)

    fun refreshSupportList() {
        performRefresh()

        waitForSupportScreenToLoad()
    }

    private fun performRefresh() {
        lastSupportRefreshTimestamp?.elapsedNow()?.let { elapsed ->
            val toWait = supportRefreshThreshold - elapsed

            if (toWait.isPositive()) {
                messages.notify(ScriptNotify.SupportListUpdatingIn(toWait))

                toWait.wait()
            }
        }

        locations.support.updateClick.click()
        Duration.seconds(1).wait()

        locations.support.updateYesClick.click()
    }

    private fun updateLastSupportRefreshTimestamp() {
        lastSupportRefreshTimestamp = TimeSource.Monotonic.markNow()
    }

    private fun isAnyDialogOpen() =
        images[Images.SupportExtra] !in locations.support.extraRegion

    private fun noMatchingSupportsPresent() =
        images[Images.SupportNotFound] in locations.support.notFoundRegion

    private fun someSupportsPresent() =
        locations.support.confirmSetupButtonRegion.exists(
            images[Images.SupportConfirmSetupButton],
            similarity = Support.supportRegionToolSimilarity
        ) || images[Images.Guest] in locations.support.friendRegion

    fun waitForSupportScreenToLoad() {
        try {
            while (true) {
                when {
                    connectionRetry.needsToRetry() -> connectionRetry.retry()
                    // wait for dialogs to close
                    isAnyDialogOpen() -> Duration.seconds(1).wait()
                    noMatchingSupportsPresent() -> {
                        updateLastSupportRefreshTimestamp()
                        performRefresh()
                    }
                    someSupportsPresent() -> return
                }

                Duration.milliseconds(100).wait()
            }
        } finally {
            updateLastSupportRefreshTimestamp()
        }
    }
}