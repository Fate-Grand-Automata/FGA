package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.seconds

/**
 * Continually opens lottery boxes until either the present box is full or there is no currency left.
 */
class AutoLottery @Inject constructor(
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager, platformImpl, fgAutomataApi.messages), IFgoAutomataApi by fgAutomataApi {
    private val spinClick = Location(834, 860)
    private val finishedLotteryBoxRegion = Region(540, 860, 140, 100)
    private val fullPresentBoxRegion = Region(1280, 720, 1280, 720)
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
        if (prefs.gameServer in listOf(GameServerEnum.Cn, GameServerEnum.Kr)) {
            throw ScriptExitException("Lottery script doesn't support this server right now.")
        }

        while (true) {
            screenshotManager.useSameSnapIn {
                when {
                    finishedLotteryBoxRegion.exists(
                        images.finishedLotteryBox,
                        Similarity = 0.65
                    ) -> reset()
                    images.presentBoxFull in fullPresentBoxRegion -> {
                        throw ScriptExitException(messages.lotteryPresentBoxFull)
                    }
                    else -> spin()
                }
            }
        }
    }
}
