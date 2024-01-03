package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.modules.ConnectionRetry
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.dagger.ScriptScope
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
        data object RanOutOfCurrency : ExitReason()
        data object PresentBoxFull : ExitReason()

        data object NoEmbersFound : ExitReason()

        data class CannotSelectAnyMore(
            val pickedStacks: Int, val pickedGoldEmbers: Int,) : ExitReason()

        data object PresentBoxFullAndCannotSelectAnymore: ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    private fun spin() {
        // Don't increase this too much or you'll regret when you're not able to stop the script
        // And your phone won't let you press anything
        locations.lottery.spinClick.click(
            prefs.lottoSpin
        )
    }

    private var pickedStacks = 0
    private var pickedGoldEmbers = 0


    private fun presentBoxFull() {
        if (prefs.receiveEmbersWhenGiftBoxFull) {
            if (prefs.isPresentBoxFull) {
                throw ExitException(ExitReason.PresentBoxFullAndCannotSelectAnymore)
            }

            val moveToPresentBox = locations.lottery.fullPresentBoxRegion
                .find(images[Images.PresentBoxFull])

            moveToPresentBox?.region?.click()

            1.seconds.wait()
            try {
                giftBox.script()
            } catch (e: AutoGiftBox.ExitException) {
                when (e.reason) {
                    AutoGiftBox.ExitReason.ReturnToLottery -> {
                        // do nothing
                    }

                    AutoGiftBox.ExitReason.NoEmbersFound -> {
                        throw ExitException(ExitReason.NoEmbersFound)
                    }

                    is AutoGiftBox.ExitReason.CannotSelectAnyMore -> {
                        // this will only execute if the loop present box is not enabled
                        throw ExitException(ExitReason.CannotSelectAnyMore(
                            e.reason.pickedStacks,
                            e.reason.pickedGoldEmbers
                        ))
                    }
                }
            }

        }
        if (prefs.loopIntoLotteryAfterPresentBox) {
            return
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
        prefs.isPresentBoxFull = false

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
