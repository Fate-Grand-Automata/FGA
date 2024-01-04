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
    api: IFgoAutomataApi,
    private val autoCEBomb: AutoCEBomb
) : EntryPoint(exitManager), IFgoAutomataApi by api {
    sealed class ExitReason {

        data object UnableVerifyIfReachedCEEnhancementMenu : ExitReason()
        data object InventoryFull : ExitReason()
        class Limit(val count: Int) : ExitReason()
    }

    class ExitException(val reason: ExitReason) : Exception()

    private var count = 0

    private fun countNext() {
        if (prefs.friendGacha.shouldLimitFP && count >= prefs.friendGacha.limitFP) {
            throw ExitException(ExitReason.Limit(count))
        }

        ++count
    }

    override fun script(): Nothing {
        if (images[Images.FriendSummon] in locations.fp.initialSummonCheck){
            locations.fp.initialSummonClick.click()
            1.seconds.wait()
            locations.fp.initialSummonContinueClick.click()
        }

        if (!isSummonButtonVisible()) {
            locations.fp.first10SummonClick.click()
            0.3.seconds.wait()
            locations.fp.okClick.click()

            countNext()
        }

        while (true) {
            if (isInventoryFull()) {
                if (prefs.friendGacha.shouldCreateCEBombAfterSummon && canGoToCeEnhancementMenu()) {
                    locations.inventoryFullRegion.click()
                    val isScreenTransitionAchieved = locations.ceBomb.getCeEnhanceRegion.exists(
                        image = images[Images.CraftEssenceEnhancement],
                        timeout = 30.seconds,
                        // due to some effects behind the word `craft` that affects the similarity
                        // lowering it would make the detection faster
                        similarity = 0.6
                    )
                    if (isScreenTransitionAchieved) {
                        autoCEBomb.script()
                    } else {
                        throw ExitException(ExitReason.UnableVerifyIfReachedCEEnhancementMenu)
                    }
                } else {
                    throw ExitException(ExitReason.InventoryFull)
                }
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

    private fun canGoToCeEnhancementMenu() = images[Images.FPCENotice] in locations.fp.ceFullVerifyRegion
}