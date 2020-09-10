package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.libautomata.*
import javax.inject.Inject
import kotlin.time.seconds

/**
 * Continually triggers 10x Summon, intended for FP summons, but could also be used for SQ summons.
 */
class AutoFriendGacha @Inject constructor(
    exitManager: ExitManager,
    platformImpl: IPlatformImpl,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager, platformImpl, fgAutomataApi.messages), IFgoAutomataApi by fgAutomataApi {
    private val first10SummonClick = Location(1400, 1120)
    private val okClick = Location(1600, 1120)
    private val continueSummonClick = Location(1600, 1325)
    private val skipRapidClick = Location(2520, 1400)

    private val continueSummonRegion = Region(1244, 1264, 580, 170)

    override fun script(): Nothing {
        if (prefs.friendPtsOnly) {
            isInFriendPtsSummon()
        }

        first10SummonClick.click()
        0.3.seconds.wait()
        okClick.click()

        while (true) {
            when {
                images.fpSummonContinue in continueSummonRegion -> {
                    continueSummonClick.click()
                    0.3.seconds.wait()
                    okClick.click()
                    if (prefs.gameServer in listOf(GameServerEnum.Tw, GameServerEnum.Kr)) {
                        0.3.seconds.wait()
                        okClick.click()
                    }
                    3.seconds.wait()
                }
                else -> skipRapidClick.click(15)
            }
        }
    }

    private fun isInFriendPtsSummon() {
        val startRightSwipeLoc = Location(300, 500)
        val endRightSwipeLoc = Location(2000, 500)

        while (images.friendSummon !in Game.friendPtSummonCheck) {
            swipe(startRightSwipeLoc, endRightSwipeLoc)
            1.0.seconds.wait()
        }
    }
}