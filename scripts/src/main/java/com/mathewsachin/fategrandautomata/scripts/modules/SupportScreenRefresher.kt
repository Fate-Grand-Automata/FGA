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

        waitForSupportScreenToLoad()
        updateLastSupportRefreshTimestamp()
    }

    private fun updateLastSupportRefreshTimestamp() {
        lastSupportRefreshTimestamp = TimeSource.Monotonic.markNow()
    }

    fun waitForSupportScreenToLoad() {
        while (true) {
            when {
                connectionRetry.needsToRetry() -> connectionRetry.retry()
                // wait for dialogs to close
                images[Images.SupportExtra] !in locations.support.extraRegion -> Duration.seconds(1).wait()
                images[Images.SupportNotFound] in locations.support.notFoundRegion -> {
                    updateLastSupportRefreshTimestamp()
                    refreshSupportList()
                    return
                }
                locations.support.confirmSetupButtonRegion.exists(
                    images[Images.SupportConfirmSetupButton],
                    similarity = Support.supportRegionToolSimilarity
                ) -> return
                images[Images.Guest] in locations.support.friendRegion -> return
            }

            Duration.milliseconds(100).wait()
        }
    }
}