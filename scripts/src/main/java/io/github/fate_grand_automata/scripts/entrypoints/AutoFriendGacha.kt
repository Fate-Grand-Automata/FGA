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

    private fun confirmInventoryFull() {
        run verify@{
            repeat(2) {
                if (connectionRetry.needsToRetry()) {
                    connectionRetry.retry()
                }
                initialFpRoll()
                val falseDetection = locations.menuScreenRegion(
                    images[Images.Menu],
                    timeout = 5.seconds,
                )
                if (falseDetection) {
                    return@verify
                }

            }
            val exist = isInMenu()
            if (exist) {
                throw ExitException(ExitReason.InventoryFull)
            }
        }
    }

    private fun initialFpRoll() {
        val initialClickLocation: Location? = if (images[Images.FriendSummon] in locations.fp.initialSummonCheck) {
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
    }

    override fun script(): Nothing {
        initialFpRoll()

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { isInventoryFull() } to {
                throw ExitException(ExitReason.InventoryFull)
            },
            { isSummonButtonVisible() } to {
                countNext()
                locations.fp.continueSummonClick.click()
                0.3.seconds.wait()
                locations.fp.okClick.click()
                3.seconds.wait()
            },
            { isInMenu() } to {
                confirmInventoryFull()
            }
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

    private fun isSummonButtonVisible() = findImage(locations.fp.continueSummonRegion, Images.FPSummonContinue)

    /**
     *  Checks if in menu.png is on the screen, indicating that a quest can be chosen.
     */
    private fun isInMenu() = images[Images.Menu] in locations.menuScreenRegion
}