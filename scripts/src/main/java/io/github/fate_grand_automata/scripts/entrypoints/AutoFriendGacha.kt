package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
import io.github.lib_automata.Location
import io.github.lib_automata.dagger.ScriptScope
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
        val initialClickLocation: Location? = if (images[Images.FriendSummon] in locations.fp.initialSummonCheck) {
            locations.fp.initialSummonClick
        } else if (images[Images.FriendSummon] in locations.fp.initial100SummonCheck) {
            locations.fp.initialSummonClick
        } else if (!isSummonButtonVisible()) {
            locations.fp.first10SummonClick
        } else null

        initialClickLocation?.let {
            it.click()
            0.3.seconds.wait()
            locations.fp.okClick.click()

            countNext()
        }

        while (true) {
            if (isInventoryFull()) {
                throw ExitException(ExitReason.InventoryFull)
            }

            if (isSummonButtonVisible()) {
                countNext()

                locations.fp.continueSummonClick.click()
                0.3.seconds.wait()
                locations.fp.okClick.click()
                3.seconds.wait()
            } else locations.fp.skipRapidClick.click(15)
        }
    }

    private fun isSummonButtonVisible() = findImage(locations.fp.continueSummonRegion, Images.FPSummonContinue)
}