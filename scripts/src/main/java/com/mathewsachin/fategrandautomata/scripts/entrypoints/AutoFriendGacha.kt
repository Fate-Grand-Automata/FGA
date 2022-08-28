package com.mathewsachin.fategrandautomata.scripts.entrypoints

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.enums.GameServerEnum
import com.mathewsachin.libautomata.EntryPoint
import com.mathewsachin.libautomata.ExitManager
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Continually triggers 10x Summon, intended for FP summons, but could also be used for SQ summons.
 */
@ScriptScope
class AutoFriendGacha @Inject constructor(
    exitManager: ExitManager,
    api: IFgoAutomataApi
) : EntryPoint(exitManager), IFgoAutomataApi by api {
    sealed class ExitReason {
        object InventoryFull : ExitReason()
        class Limit(val count: Int) : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    private var count = 0

    private fun countNext() {
        if (prefs.shouldLimitFP && count >= prefs.limitFP) {
            throw ExitException(ExitReason.Limit(count))
        }

        ++count
    }

    override fun script(): Nothing {
        while (true) {
            if (images[Images.FPSummonContinue] in locations.fp.continueSummonRegion) {
                locations.fp.continueSummonClick.click()
            } else {
                locations.fp.first10SummonClick.click()
            }
            // wait for confirm dialog
            1.seconds.wait()

            if (prefs.gameServer in listOf(GameServerEnum.En, GameServerEnum.Jp, GameServerEnum.Kr)
                && locations.inventoryFullRegion.exists(images[Images.InventoryFull], similarity = 0.23)) {
                throw ExitException(ExitReason.InventoryFull)
            }
            locations.fp.okClick.click()

            countNext()

            // wait for progress
            3.seconds.wait()

            // in summoning progress, click much for skip
            locations.fp.skipRapidClick.click(15)
        }
    }
}
