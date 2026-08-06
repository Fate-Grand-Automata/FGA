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
    private var isInLoop = false
    private var initialScreen = false

    private fun countNext() {
        if (prefs.shouldLimitFP && count >= prefs.limitFP) {
            throw ExitException(ExitReason.Limit(count))
        }

        ++count
    }

    override fun script(): Nothing {
        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { isInventoryFull() } to {
                throw ExitException(ExitReason.InventoryFull)
            },
            { isFirstSummon() } to {
                // First 10x or 100x Summon
                locations.fp.first10SummonClick.click()
                0.3.seconds.wait()
                locations.fp.okClick.click()

                initialScreen = true
            },
            { isDailyFree() } to {
                // Daily Free Summon
                locations.fp.initialSummonClick.click()
                0.3.seconds.wait()
                locations.fp.initialSummonContinueClick.click()

                initialScreen = true
            },
            { isSummonButtonVisible() } to { rollFPAgain() }
        )

        while (true) {
            val actor = useSameSnapIn {
                screens
                    .asSequence()
                    .filter { (validator, _) -> validator() }
                    .map { (_, actor) -> actor }
                    .firstOrNull()
            } ?: { locations.fp.skipRapidClick.click(15) }
            actor.invoke()
            0.5.seconds.wait()
        }
    }

    private fun rollFPAgain() {
        if (!isInLoop && initialScreen) {
            countNext()
            isInLoop = true
        }
        countNext()

        locations.fp.continueSummonClick.click()
        0.3.seconds.wait()
        locations.fp.okClick.click()
        3.seconds.wait()
    }

    private fun isSummonButtonVisible() = findImage(locations.fp.continueSummonRegion, Images.FPSummonContinue)

    private fun isFirstSummon(): Boolean {
        if (isInLoop) return false

        return images[Images.FriendSummon] in locations.fp.summonCheck
    }

    private fun isDailyFree(): Boolean {
        if (isInLoop) return false

        return images[Images.FriendSummon] in locations.fp.initialSummonCheck
    }
}