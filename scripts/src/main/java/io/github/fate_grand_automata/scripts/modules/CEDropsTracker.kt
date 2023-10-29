package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.ScriptNotify
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.models.battle.BattleState
import io.github.lib_automata.Region
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class CEDropsTracker @Inject constructor(
    api: IFgoAutomataApi,
    private val state: BattleState
) : IFgoAutomataApi by api {
    var count = 0
        private set

    fun autoDecrement() {
//        val refill = prefs.refill
        val perServerConfigPref = prefs.selectedServerConfigPref

        // Auto-decrement CEs
        if (perServerConfigPref.shouldLimitCEs) {
            perServerConfigPref.limitCEs -= count

            // Turn off limit by CEs when done
            if (perServerConfigPref.limitCEs <= 0) {
                perServerConfigPref.limitCEs = 1
                perServerConfigPref.shouldLimitCEs = false
            }
        }
    }

    fun lookForCEDrops() {
        val starsRegion = Region(40, -40, 80, 40)

        val ceDropped = locations.scriptArea
            .findAll(images[Images.DropCE])
            .map { (region, _) ->
                starsRegion + region.location
            }
            .count { images[Images.DropCEStars] in it }

        if (ceDropped > 0) {
            count += ceDropped

            if (prefs.selectedServerConfigPref.shouldLimitCEs && count >= prefs.selectedServerConfigPref.limitCEs) {
                // Count the current run
                state.nextRun()

                throw AutoBattle.BattleExitException(AutoBattle.ExitReason.LimitCEs(count))
            } else messages.notify(ScriptNotify.CEDropped)
        }
    }
}