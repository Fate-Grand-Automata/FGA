package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.locations.LotteryLocations
import com.mathewsachin.fategrandautomata.scripts.modules.ConnectionRetry
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

/**
 * Continually opens lottery boxes until either the present box is full or there is no currency left.
 */
@ScriptScope
class AutoLottery @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi,
    private val giftBox: AutoGiftBox,
    private val connectionRetry: ConnectionRetry,
    private val locations: LotteryLocations
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    sealed class ExitReason {
        object ResetDisabled: ExitReason()
        object PresentBoxFull: ExitReason()
    }

    class ExitException(val reason: ExitReason): Exception()

    private fun spin() {
        // Don't increase this too much or you'll regret when you're not able to stop the script
        // And your phone won't let you press anything
        locations.spinClick.click(25)
    }

    private fun reset() {
        if (prefs.preventLotteryBoxReset) {
            throw ExitException(ExitReason.ResetDisabled)
        }

        locations.resetClick.click()
        Duration.seconds(0.5).wait()

        locations.resetConfirmationClick.click()
        Duration.seconds(3).wait()

        locations.resetCloseClick.click()
        Duration.seconds(2).wait()
    }

    private fun presentBoxFull() {
        if (prefs.receiveEmbersWhenGiftBoxFull) {
            val moveToPresentBox = locations.fullPresentBoxRegion
                .find(images[Images.PresentBoxFull])

            moveToPresentBox?.region?.click()

            Duration.seconds(1).wait()
            giftBox.script()
        }

        throw ExitException(ExitReason.PresentBoxFull)
    }

    override fun script(): Nothing {
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { images[Images.LotteryBoxFinished] in locations.finishedRegion } to { reset() },
            { connectionRetry.needsToRetry() } to { connectionRetry.retry() },
            { images[Images.PresentBoxFull] in locations.fullPresentBoxRegion } to { presentBoxFull() }
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
