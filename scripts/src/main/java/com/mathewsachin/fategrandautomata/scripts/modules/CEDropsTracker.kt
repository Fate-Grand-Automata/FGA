package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.ScriptNotify
import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.libautomata.Region
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class CEDropsTracker @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val state: BattleState
) : IFgoAutomataApi by fgAutomataApi {
    var count = 0
        private set

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

            if (prefs.stopOnCEDrop) {
                // Count the current run
                state.nextRun()

                throw AutoBattle.BattleExitException(AutoBattle.ExitReason.CEDropped)
            } else messages.notify(ScriptNotify.CEDropped)
        }
    }
}