package io.github.fate_grand_automata.scripts.entrypoints

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.lib_automata.EntryPoint
import io.github.lib_automata.ExitManager
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

        data object RunOutOfFP : ExitReason()

        data class SellBannerNotVisible(val count: Int = 0, val inventoryFull: Boolean = false) : ExitReason()

        data class ReachedSellBanner(val count: Int = 0, val inventoryFull: Boolean = false) : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    private var count = 0

    private fun countNext() {
        if (prefs.friendGacha.shouldLimitFP && count >= prefs.friendGacha.limitFP) {
            if (prefs.friendGacha.shouldRedirectToSell) {
                locations.fp.fpSellRegion.click()
                waitForSellBanner(count)
            } else {
                throw ExitException(ExitReason.Limit(count))
            }
        }

        ++count
    }

    override fun script(): Nothing {
        if (images[Images.FriendSummon] in locations.fp.initialSummonCheck) {
            locations.fp.initialSummonClick.click()
            0.3.seconds.wait()
            locations.fp.initialSummonContinueClick.click()

            countNext()
        } else if (!isSummonButtonVisible()) {
            locations.fp.first10SummonClick.click()
            0.3.seconds.wait()
            locations.fp.okClick.click()

            countNext()
        }

        val screens: Map<() -> Boolean, () -> Unit> = mapOf(
            { isInventoryFull() } to {
                performActionsOnInventoryFull()
            },
            { isSummonButtonVisible() } to {
                rollFPAgain()
            },
            { isFPSellVisible() } to {
                if (prefs.friendGacha.shouldRedirectToSell) {
                    locations.fp.fpSellRegion.click()
                    waitForSellBanner()
                } else {
                    throw ExitException(ExitReason.RunOutOfFP)
                }
            },
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
        countNext()

        locations.fp.continueSummonClick.click()
        0.3.seconds.wait()
        locations.fp.okClick.click()
        3.seconds.wait()
    }

    private fun performActionsOnInventoryFull() {

        when {
            prefs.friendGacha.shouldRedirectToSell -> {
                locations.fp.inventoryFullSellRegion.click()
                waitForSellBanner(inventoryFull = true)
            }

            else -> throw ExitException(ExitReason.InventoryFull)
        }

    }

    private fun waitForSellBanner(count: Int = 0, inventoryFull: Boolean = false) {
        when (waitUntilSellBannerExist()) {
            true -> throw ExitException(ExitReason.ReachedSellBanner(count, inventoryFull))
            false -> throw ExitException(ExitReason.SellBannerNotVisible(count, inventoryFull))
        }
    }

    private fun isSummonButtonVisible() = findImage(locations.fp.continueSummonRegion, Images.FPSummonContinue)

    private fun isFPSellVisible() = images[Images.FPSell] in locations.fp.fpSellRegion

    private fun waitUntilSellBannerExist() = locations.fp.sellBannerRegion.exists(
        image = images[Images.CEDetails],
        timeout = 30.seconds,
    )
}