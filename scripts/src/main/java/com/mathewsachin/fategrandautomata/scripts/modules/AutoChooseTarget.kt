package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.Images
import com.mathewsachin.fategrandautomata.scripts.models.EnemyTarget
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class AutoChooseTarget @Inject constructor(
    api: IFgoAutomataApi,
    private val state: BattleState
) : IFgoAutomataApi by api {
    private fun isPriorityTarget(enemy: EnemyTarget): Boolean {
        val region = locations.battle.dangerRegion(enemy)

        val isDanger = images[Images.TargetDanger] in region
        val isServant = images[Images.TargetServant] in region

        return isDanger || isServant
    }

    private fun chooseTarget(enemy: EnemyTarget) {
        locations.battle.locate(enemy).click()

        0.5.seconds.wait()

        locations.battle.extraInfoWindowCloseClick.click()
    }

    fun choose() {
        // from my experience, most boss stages are ordered like(Servant 1)(Servant 2)(Servant 3),
        // where(Servant 3) is the most powerful one. see docs/ boss_stage.png
        // that's why the table is iterated backwards.

        val dangerTarget = EnemyTarget.list
            .lastOrNull { isPriorityTarget(it) }

        if (dangerTarget != null && state.chosenTarget != dangerTarget) {
            chooseTarget(dangerTarget)
        }

        state.chosenTarget = dangerTarget
    }
}