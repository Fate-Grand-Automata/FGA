package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.fategrandautomata.scripts.models.battle.BattleState
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoSkill @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val state: BattleState,
    private val skillCommand: AutoSkillCommand,
    private val caster: Caster
) : IFgoAutomataApi by fgAutomataApi {
    private fun act(action: AutoSkillAction) = when (action) {
        is AutoSkillAction.Atk -> state.atk = action
        is AutoSkillAction.ServantSkill -> caster.castServantSkill(action.skill, action.target)
        is AutoSkillAction.MasterSkill -> caster.castMasterSkill(action.skill, action.target)
        is AutoSkillAction.TargetEnemy -> caster.selectEnemyTarget(action.enemy)
        is AutoSkillAction.OrderChange -> caster.orderChange(action)
    }

    fun execute() {
        val commandList = skillCommand[state.stage, state.turn]

        if (commandList.isNotEmpty()) {
            for (action in commandList) {
                act(action)
            }
        }
    }
}