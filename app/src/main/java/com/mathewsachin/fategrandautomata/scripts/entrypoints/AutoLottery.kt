package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.core.*
import com.mathewsachin.fategrandautomata.scripts.ImageLocator
import com.mathewsachin.fategrandautomata.scripts.modules.initScaling
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
        spinClick.click(480)
    }

    private fun reset() {
        resetClick.click()
        AutomataApi.wait(0.5.seconds)

        resetConfirmationClick.click()
        AutomataApi.wait(3.seconds)

        resetCloseClick.click()
        AutomataApi.wait(2.seconds)
    }

    override fun script(): Nothing {
        initScaling()

        while (true) {
            when {
                finishedLotteryBoxRegion.exists(
                    ImageLocator.FinishedLotteryBox,
                    Similarity = 0.65
                ) -> {
                    reset()
                }
                fullPresentBoxRegion.exists(ImageLocator.PresentBoxFull) -> {
                    throw ScriptExitException("Present Box Full")
                }
                else -> spin()
            }
        }
    }
}