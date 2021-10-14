package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration

@ScriptScope
class Withdraw @Inject constructor(
    fgAutomataApi: IFgoAutomataApi
) : IFgoAutomataApi by fgAutomataApi {
    var count = 0
        private set

    /**
     * Checks if the window for withdrawing from the battle exists.
     */
    fun needsToWithdraw() =
        images[Images.Withdraw] in game.withdrawRegion

    /**
     * Handles withdrawing from battle. Depending on whether withdraw is enabled, the script either
     * withdraws automatically or stops completely.
     */
    fun withdraw() {
        if (!prefs.withdrawEnabled) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.WithdrawDisabled)
        }

        // Withdraw Region can vary depending on if you have Command Spells/Quartz
        val withdrawRegion = game.withdrawRegion.find(images[Images.Withdraw])
            ?: return

        withdrawRegion.region.click()

        Duration.seconds(0.5).wait()

        // Click the "Accept" button after choosing to withdraw
        game.withdrawAcceptClick.click()

        Duration.seconds(1).wait()

        // Click the "Close" button after accepting the withdrawal
        game.withdrawCloseClick.click()

        ++count
    }
}