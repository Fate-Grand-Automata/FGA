package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import javax.inject.Inject
import kotlin.time.seconds

/**
 * Continually triggers 10x Summon, intended for FP summons, but could also be used for SQ summons.
 */
class AutoFriendGacha @Inject constructor(
    exitManager: ExitManager,
    fgAutomataApi: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by fgAutomataApi {
    override fun script(): Nothing {
        if (images.fpSummonContinue !in game.fpContinueSummonRegion) {
            game.fpFirst10SummonClick.click()
            0.3.seconds.wait()
            game.fpOkClick.click()
        }

        while (true) {
            if (images.fpSummonContinue in game.fpContinueSummonRegion) {
                game.fpContinueSummonClick.click()
                0.3.seconds.wait()
                game.fpOkClick.click()
                3.seconds.wait()
            } else game.fpSkipRapidClick.click(15)
        }
    }
}