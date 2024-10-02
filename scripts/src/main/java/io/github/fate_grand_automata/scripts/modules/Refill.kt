package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptNotify
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.prefs.IPerServerConfigPrefs
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
    private fun refillOnce(
        isLastRun: Boolean = false
    ) {
        val perServerConfigPref = prefs.selectedServerConfigPref

        when {
            /**
             * If the user has resources to refill and has the wait for AP regen option enabled
             * and this is the last run, wait for AP regen instead of refilling.
             */
            perServerConfigPref.waitForAPRegen && isLastRun -> waitForAPRegen()
            /**
             * If the user has resources to refill and has not reached the current apple count,
             */
            perServerConfigPref.resources.isNotEmpty() && timesRefilled < perServerConfigPref.currentAppleCount -> {
                refillAP(perServerConfigPref = perServerConfigPref)
            }
            /**
             * wait for AP regen if the user has the wait for AP regen option enabled
             */

            perServerConfigPref.waitForAPRegen -> waitForAPRegen()

            else -> throw AutoBattle.BattleExitException(AutoBattle.ExitReason.APRanOut)
        }
    }

    private fun refillAP(perServerConfigPref: IPerServerConfigPrefs) {
        //TODO check for OK image between each resource
        perServerConfigPref.resources
            .flatMap { locations.locate(it) }
            .forEach { it.click() }

        1.seconds.wait()
        locations.staminaOkClick.click()
        ++timesRefilled

        3.seconds.wait()
    }

    private fun waitForAPRegen() {
        locations.staminaCloseClick.click()

        messages.notify(ScriptNotify.WaitForAPRegen())

        60.seconds.wait()
    }

    fun refill(
        isLastRun: Boolean = false
    ) {
        if (images[Images.Stamina] in locations.staminaScreenRegion) {
            refillOnce(
                isLastRun = isLastRun
            )
        }
    }

    fun autoDecrement() {
        val perServerConfigPref = prefs.selectedServerConfigPref
        // Auto-decrement apples
        perServerConfigPref.currentAppleCount -= timesRefilled

    }
}