package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.modules.needsToRetry
import com.mathewsachin.fategrandautomata.scripts.modules.retry
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import javax.inject.Inject
import kotlin.time.Duration

/**
 * Continually opens lottery boxes until either the present box is full or there is no currency left.
 */
class AutoLottery @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    sealed class ExitReason {
        object ResetDisabled: ExitReason()
        object PresentBoxFull: ExitReason()
    }

    class ExitException(val reason: ExitReason): Exception()

    private fun spin() {
        // Don't increase this too much or you'll regret when you're not able to stop the script
        // And your phone won't let you press anything
        game.lotterySpinClick.click(25)
    }

    private fun reset() {
        if (prefs.preventLotteryBoxReset) {
            throw ExitException(ExitReason.ResetDisabled)
        }

        game.lotteryResetClick.click()
        Duration.seconds(0.5).wait()

        game.lotteryResetConfirmationClick.click()
        Duration.seconds(3).wait()

        game.lotteryResetCloseClick.click()
        Duration.seconds(2).wait()
    }

    override fun script(): Nothing {
        while (true) {
            useSameSnapIn {
                when {
                    images[Images.LotteryBoxFinished] in game.lotteryFinishedRegion -> reset()
                    images[Images.PresentBoxFull] in game.lotteryFullPresentBoxRegion -> {
                        throw ExitException(ExitReason.PresentBoxFull)
                    }
                    needsToRetry() -> retry()
                    else -> spin()
                }
            }
        }
    }
}
