package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.fategrandautomata.scripts.modules.Game
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.Location
import javax.inject.Inject
import kotlin.time.seconds

/**
 * Continually triggers 10x Summon, intended for FP summons, but could also be used for SQ summons.
 */
class AutoFriendGacha @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    private val first10SummonClick = Location(1400, 1120)
    private val okClick = Location(1600, 1120)
    private val continueSummonClick = Location(1600, 1325)
    private val skipRapidClick = Location(2520, 1400)

    override fun script(): Nothing {
        if (images.fpSummonContinue !in Game.continueSummonRegion) {
            first10SummonClick.click()
            0.3.seconds.wait()
            okClick.click()
        }

        while (true) {
            if (images.fpSummonContinue in Game.continueSummonRegion) {
                continueSummonClick.click()
                0.3.seconds.wait()
                okClick.click()
                3.seconds.wait()
            } else skipRapidClick.click(15)
        }
    }
}