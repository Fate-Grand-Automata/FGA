package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.Images
import io.github.fate_grand_automata.scripts.models.EnemyTarget
import io.github.fate_grand_automata.scripts.models.battle.BattleState
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ScriptScope
class AutoChooseTarget @Inject constructor(
    api: IFgoAutomataApi,
    private val state: BattleState
) : IFgoAutomataApi by api {
    private fun isPriorityTarget(enemy: EnemyTarget): Boolean {
        val region = locations.battle.dangerRegion(enemy)

        val normalFormation = enemy in EnemyTarget.threeEnemyFormationList

        val isDanger = (if (normalFormation) images[Images.TargetDanger] else
            images[Images.TargetDangerSix]) in region

        val isServant = (if (normalFormation) images[Images.TargetServant] else
            images[Images.TargetServantSix]) in region

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