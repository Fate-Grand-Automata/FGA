package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptNotify
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.enums.RefillResourceEnum
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class Refill @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {
    var timesRefilled = 0
        private set

    /**
     * Refills the AP with apples depending on preferences.
     * If needed, loops and wait for AP regeneration
     */
    private fun refillOnce() {
        val perServerConfigPref = prefs.selectedServerConfigPref

        if (perServerConfigPref.resources.isNotEmpty()
            && timesRefilled < perServerConfigPref.currentAppleCount
        ) {
            //TODO check for OK image between each resource
            val resource = perServerConfigPref.resources.first()
            locations.locate(resource)
                .forEach {
                    it.click()
                }

            1.seconds.wait()

            val staminaRefill = when {
                resource == RefillResourceEnum.Gold -> 1
                resource == RefillResourceEnum.SQ -> 1
                perServerConfigPref.staminaOverRecharge -> {
                    var refill = checkMaxRefillAmount()
                    // If the refill amount is more than the current apple count,
                    // refill the minimum amount only.
                    if (refill + timesRefilled > perServerConfigPref.currentAppleCount) {
                        refill = checkMinRefillAmount()
                    } else {
                        locations.staminaOverRechargeRegion.click()
                    }
                    refill
                }
                else -> checkMinRefillAmount()
            }
            0.5.seconds.wait()

            locations.staminaOkClick.click()
            timesRefilled += staminaRefill

            3.seconds.wait()
        } else if (perServerConfigPref.waitForAPRegen) {
            locations.staminaCloseClick.click()

            messages.notify(ScriptNotify.WaitForAPRegen())

            60.seconds.wait()
        } else throw AutoBattle.BattleExitException(AutoBattle.ExitReason.APRanOut)
    }

    private fun checkMinRefillAmount(): Int {
        val staminaMinText = locations.staminaMinRegion.detectNumberFontText()
        val regex = Regex("""(\d+)""")
        val staminaMin = regex.find(staminaMinText)?.groupValues?.getOrNull(1)?.toInt()
        return staminaMin ?: 1
    }

    private fun checkMaxRefillAmount(): Int {
        val staminaMaxText = locations.staminaMaxRegion.detectNumberFontText()
        val regex = Regex("""(\d+)""")
        val staminaMax = regex.find(staminaMaxText)?.groupValues?.getOrNull(1)?.toInt()
        return staminaMax ?: 1
    }

    fun refill() {
        if (images[Images.Stamina] in locations.staminaScreenRegion) {
            refillOnce()
        }
    }

    fun autoDecrement() {
        val perServerConfigPref = prefs.selectedServerConfigPref
        // Auto-decrement apples
        perServerConfigPref.currentAppleCount -= timesRefilled

    }
}