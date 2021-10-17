package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.IFgoAutomataApi
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillCommand
import com.mathewsachin.libautomata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class AutoSkill @Inject constructor(
    fgAutomataApi: IFgoAutomataApi,
    private val skillCommand: AutoSkillCommand,
    private val caster: Caster
) : IFgoAutomataApi by fgAutomataApi {
    private fun act(action: AutoSkillAction) = when (action) {
        is AutoSkillAction.Atk -> { }
        is AutoSkillAction.ServantSkill -> caster.castServantSkill(action.skill, action.target)
        is AutoSkillAction.MasterSkill -> caster.castMasterSkill(action.skill, action.target)
        is AutoSkillAction.TargetEnemy -> caster.selectEnemyTarget(action.enemy)
        is AutoSkillAction.OrderChange -> caster.orderChange(action)
    }

    fun execute(stage: Int, turn: Int): AutoSkillAction.Atk {
        val commandList = skillCommand[stage, turn]
        var atk = AutoSkillAction.Atk.noOp()

        if (commandList.isNotEmpty()) {
            for (action in commandList) {
                if (action is AutoSkillAction.Atk) {
                    atk = action
                }

                act(action)
            }
        }

        return atk
    }
}