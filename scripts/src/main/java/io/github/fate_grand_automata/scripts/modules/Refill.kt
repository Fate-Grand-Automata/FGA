package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptNotify
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
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
        val refill = prefs.refill

        if (refill.resources.isNotEmpty()
            && timesRefilled < refill.repetitions
        ) {
            //TODO check for OK image between each resource
            refill.resources
                .flatMap { locations.locate(it) }
                .forEach { it.click() }

            1.seconds.wait()
            locations.staminaOkClick.click()
            ++timesRefilled

            3.seconds.wait()
        } else if (prefs.waitAPRegen) {
            locations.staminaCloseClick.click()

            messages.notify(ScriptNotify.WaitForAPRegen())

            60.seconds.wait()
        } else throw AutoBattle.BattleExitException(AutoBattle.ExitReason.APRanOut)
    }

    fun refill() {
        if (images[Images.Stamina] in locations.staminaScreenRegion) {
            refillOnce()
        }
    }

    fun autoDecrement() {
        val refill = prefs.refill

        // Auto-decrement apples
        refill.repetitions -= timesRefilled
    }
}