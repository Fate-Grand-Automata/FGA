package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.enums.GameServer
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
        object RanOutOfCurrency : ExitReason()
        object PresentBoxFull : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    var lottoLongPress = 20

    private fun spin() {
        // Don't increase this too much or you'll regret when you're not able to stop the script
        // And your phone won't let you press anything
        locations.lottery.spinClick.click(20)
    }

    private fun spinLongClick() {
        locations.lottery.spinClick.longPress(lottoLongPress * 1_000)
    }

    /**
     * Switch between the two different spin methods depending on the server
     */
    private fun spinGameServer() = when (prefs.gameServer) {
        is GameServer.Jp -> spinLongClick()
        else -> spin()
    }

    private fun presentBoxFull() {
        if (prefs.receiveEmbersWhenGiftBoxFull) {
            val moveToPresentBox = locations.lottery.fullPresentBoxRegion
                .find(images[Images.PresentBoxFull])

            moveToPresentBox?.region?.click()

            3.seconds.wait()
            giftBox.script()
        }

        throw ExitException(ExitReason.PresentBoxFull)
    }

    private fun isTransition() = images[Images.LotteryTransition] in locations.lottery.transitionRegion


    private fun isLotteryDone() = locations.lottery.doneRegion.exists(
        images[Images.LotteryBoxFinished],
        similarity = 0.85
    )

    private fun confirmIfLotteryDone() {
        run verify@{
            repeat(2) {
                if (connectionRetry.needsToRetry()) {
                    connectionRetry.retry()
                }
                spin()
                val falseDetection = locations.lottery.doneRegion.waitVanish(
                    images[Images.LotteryBoxFinished],
                    timeout = 5.seconds,
                    similarity = 0.85
                )
                if (falseDetection) {
                    return@verify
                }
            }
            val exist = isLotteryDone()
            if (exist) {
                throw ExitException(ExitReason.RanOutOfCurrency)
            }
        }
    }

    override fun script(): Nothing {
        lottoLongPress = prefs.lottoLongPress

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { images[Images.PresentBoxFull] in locations.lottery.fullPresentBoxRegion } to { presentBoxFull() },
            { isLotteryDone() } to { confirmIfLotteryDone() },
            { isTransition() } to { locations.lottery.transitionRegion.click() }
        )

        while (true) {
            val actor = useSameSnapIn {
                screens
                    .asSequence()
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            } ?: { spinGameServer() }

            actor.invoke()
        }
    }
}
