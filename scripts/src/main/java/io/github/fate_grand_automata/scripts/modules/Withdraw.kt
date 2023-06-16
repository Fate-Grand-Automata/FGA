package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class Withdraw @Inject constructor(
    api: IFgoAutomataApi
) : IFgoAutomataApi by api {
    var count = 0
        private set

    /**
     * Checks if the window for withdrawing from the battle exists.
     */
    fun needsToWithdraw() =
        images[Images.Withdraw] in locations.withdrawRegion

    /**
     * Handles withdrawing from battle. Depending on whether withdraw is enabled, the script either
     * withdraws automatically or stops completely.
     */
    fun withdraw() {
        if (!prefs.withdrawEnabled) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.WithdrawDisabled)
        }

        // Withdraw Region can vary depending on if you have Command Spells/Quartz
        val withdrawRegion = locations.withdrawRegion.find(images[Images.Withdraw])
            ?: return

        withdrawRegion.region.click()

        0.5.seconds.wait()

        // Click the "Accept" button after choosing to withdraw
        locations.withdrawAcceptClick.click()

        1.seconds.wait()

        // Click the "Close" button after accepting the withdrawal
        locations.withdrawCloseClick.click()

        ++count
    }
}