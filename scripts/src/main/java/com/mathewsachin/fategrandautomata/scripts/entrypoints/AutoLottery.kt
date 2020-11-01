package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.seconds

/**
 * Continually opens lottery boxes until either the present box is full or there is no currency left.
 */
class AutoLottery @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    private val spinClick = Location(834, 860)

    private val fullPresentBoxRegion = Region(1300, 860, 1000, 500)
    private val resetClick = Location(2200, 480)
    private val resetConfirmationClick = Location(1774, 1122)
    private val resetCloseClick = Location(1270, 1120)

    private fun spin() {
        // Don't increase this too much or you'll regret when you're not able to stop the script
        // And your phone won't let you press anything
        spinClick.click(25)
    }

    private fun reset() {
        resetClick.click()
        0.5.seconds.wait()

        resetConfirmationClick.click()
        3.seconds.wait()

        resetCloseClick.click()
        2.seconds.wait()
    }

    override fun script(): Nothing {
        while (true) {
            useSameSnapIn {
                when {
                    images.finishedLotteryBox in Game.finishedLotteryBoxRegion -> reset()
                    images.presentBoxFull in fullPresentBoxRegion -> {
                        throw ScriptExitException(messages.lotteryPresentBoxFull)
                    }
                    else -> spin()
                }
            }
        }
    }
}
