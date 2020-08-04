package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFGAutomataApi
import com.mathewsachin.libautomata.*
import kotlin.time.seconds

/**
 * Continually triggers 10x Summon, intended for FP summons, but could also be used for SQ summons.
 */
class AutoFriendGacha(
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFGAutomataApi
) : EntryPoint(exitManager, platformImpl), IFGAutomataApi by fgAutomataApi {
    private val first10SummonClick = Location(1400, 1120)
    private val okClick = Location(1600, 1120)
    private val continueSummonClick = Location(1600, 1420)
    private val skipRapidClick = Location(2520, 1400)

    private val continueSummonRegion = Region(1244, 1264, 580, 170)

    override fun script(): Nothing {
        scaling.init()

        if (prefs.friendPtsOnly) {
            isInFriendPtsSummon()
        }

        first10SummonClick.click()
        0.3.seconds.wait()
        okClick.click()

        while (true) {
            when {
                continueSummonRegion.exists(images.fpSummonContinue) -> {
                    continueSummonClick.click()
                    0.3.seconds.wait()
                    okClick.click()
                    3.seconds.wait()
                }
                else -> skipRapidClick.click(15)
            }
        }
    }

    private fun isInFriendPtsSummon() {
        val startRightSwipeLoc = Location(300, 500)
        val endRightSwipeLoc = Location(2000, 500)

        while (!game.friendPtSummonCheck.exists(images.friendSummon)) {
            swipe(startRightSwipeLoc, endRightSwipeLoc)
            1.0.seconds.wait()
        }
    }
}