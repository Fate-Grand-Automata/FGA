package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class Refill @Inject constructor(
    fgAutomataApi: IFgoAutomataApi
) : IFgoAutomataApi by fgAutomataApi {
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
            refill.resources
                .map { game.locate(it) }
                .forEach { it.click() }

            Duration.seconds(1).wait()
            game.staminaOkClick.click()
            ++timesRefilled

            Duration.seconds(3).wait()
        } else if (prefs.waitAPRegen) {
            game.staminaCloseClick.click()

            messages.notify(ScriptNotify.WaitForAPRegen())

            Duration.seconds(60).wait()
        } else throw AutoBattle.BattleExitException(AutoBattle.ExitReason.APRanOut)
    }

    fun refill() {
        while (images[Images.Stamina] in game.staminaScreenRegion) {
            refillOnce()
        }
    }

    fun autoDecrement() {
        val refill = prefs.refill

        // Auto-decrement apples
        refill.repetitions -= timesRefilled
    }
}