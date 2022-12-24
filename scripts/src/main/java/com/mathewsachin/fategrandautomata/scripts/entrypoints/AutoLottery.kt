package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.modules.ConnectionRetry
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Continually opens lottery boxes until either the present box is full or there is no currency left.
 */
@ScriptScope
class AutoLottery @Inject constructor(
    exitManager: ExitManager,
    api: IFgoAutomataApi,
    private val giftBox: AutoGiftBox,
    private val connectionRetry: ConnectionRetry
) : EntryPoint(exitManager), IFgoAutomataApi by api {
    sealed class ExitReason {
        object RanOutOfCurrency : ExitReason()
        object PresentBoxFull : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    private fun spin() {
        // Don't increase this too much or you'll regret when you're not able to stop the script
        // And your phone won't let you press anything
        locations.lottery.spinClick.click(20)
    }

    private fun presentBoxFull() {
        if (prefs.receiveEmbersWhenGiftBoxFull) {
            val moveToPresentBox = locations.lottery.fullPresentBoxRegion
                .find(images[Images.PresentBoxFull])

            moveToPresentBox?.region?.click()

            1.seconds.wait()
            giftBox.script()
        }

        throw ExitException(ExitReason.PresentBoxFull)
    }

    private fun isOutOfCurrency() = images[Images.LotteryBoxFinished] in locations.lottery.finishedRegion

    private fun ranOutOfCurrency() {
        // this can also be triggered before the notification about a new box happens
        // tap any dialog away, then check for the message
        spin()
        if (isNewLineup()) {
            confirmNewLineup()
        } else if (isOutOfCurrency()) {
            throw ExitException(ExitReason.RanOutOfCurrency)
        }
    }

    private fun isNewLineup() =
        images[Images.LotteryLineupUpdated] in locations.lottery.lineupUpdatedRegion

    private fun confirmNewLineup() {
        locations.lottery.confirmNewLineupClick.click()
    }

    override fun script(): Nothing {
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { isNewLineup() } to { confirmNewLineup() },
            { isOutOfCurrency() } to { ranOutOfCurrency() },
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { images[Images.PresentBoxFull] in locations.lottery.fullPresentBoxRegion } to { presentBoxFull() }
        )

        while (true) {
            val actor = useSameSnapIn {
                screens
                    .asSequence()
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            } ?: { spin() }

            actor.invoke()
        }
    }
}
