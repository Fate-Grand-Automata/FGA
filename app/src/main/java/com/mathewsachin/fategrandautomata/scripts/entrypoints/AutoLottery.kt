package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.core.*
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.modules.initScaling
import com.mathewsachin.fategrandautomata.scripts.prefs.Preferences
import kotlin.time.seconds

/**
 * Continually opens lottery boxes until either the present box is full or there is no currency left.
 */
class AutoLottery : EntryPoint() {
    private val spinClick = Location(834, 860)
    private val finishedLotteryBoxRegion = Region(575, 860, 70, 100)
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
        when (Preferences.GameServer) {
            GameServerEnum.Cn, GameServerEnum.Tw -> {
                throw ScriptExitException("Lottery script doesn't support CN and TW servers right now.")
            }
        }

        initScaling()

        while (true) {
            ScreenshotManager.useSameSnapIn {
                when {
                    finishedLotteryBoxRegion.exists(
                        ImageLocator.FinishedLotteryBox,
                        Similarity = 0.65
                    ) -> reset()
                    fullPresentBoxRegion.exists(ImageLocator.PresentBoxFull) -> {
                        throw ScriptExitException("Present Box Full")
                    }
                    else -> spin()
                }
            }
        }
    }
}