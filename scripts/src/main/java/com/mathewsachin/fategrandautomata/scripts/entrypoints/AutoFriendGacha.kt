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
    sealed class ExitReason {
        object InventoryFull: ExitReason()
        class Limit(val count: Int): ExitReason()
    }

    class ExitException(val reason: ExitReason): Exception()

    private var count = 0

    private fun countNext() {
        if (prefs.shouldLimitFP && count >= prefs.limitFP) {
            throw ExitException(ExitReason.Limit(count))
        }

        ++count
    }

    override fun script(): Nothing {
        if (images.fpSummonContinue !in game.fpContinueSummonRegion) {
            game.fpFirst10SummonClick.click()
            0.3.seconds.wait()
            game.fpOkClick.click()

            countNext()
        }

        while (true) {
            if (isInventoryFull()) {
                throw ExitException(ExitReason.InventoryFull)
            }

            if (images.fpSummonContinue in game.fpContinueSummonRegion) {
                countNext()

                game.fpContinueSummonClick.click()
                0.3.seconds.wait()
                game.fpOkClick.click()
                3.seconds.wait()
            } else game.fpSkipRapidClick.click(15)
        }
    }
}