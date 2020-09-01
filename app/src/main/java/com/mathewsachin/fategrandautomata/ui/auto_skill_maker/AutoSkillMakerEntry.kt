package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.ServantTarget
import com.mathewsachin.fategrandautomata.scripts.models.Skill

sealed class AutoSkillMakerEntry {
    class Action(val action: AutoSkillAction) : AutoSkillMakerEntry() {
        private fun toString(skill: Skill, target: ServantTarget?) =
            if (target == null)
                "${skill.autoSkillCode}"
            else "${skill.autoSkillCode}${target.autoSkillCode}"

        override fun toString() = when (action) {
            is AutoSkillAction.CardsBeforeNP -> "n${action.count}"
            is AutoSkillAction.NP -> "${action.np.autoSkillCode}"
            is AutoSkillAction.ServantSkill -> toString(action.skill, action.target)
            is AutoSkillAction.MasterSkill -> toString(action.skill, action.target)
            is AutoSkillAction.TargetEnemy -> "t${action.enemy.autoSkillCode}"
            is AutoSkillAction.OrderChange -> "x${action.starting.autoSkillCode}${action.sub.autoSkillCode}"
            AutoSkillAction.NoOp -> "0"
        }
    }

    object Start : AutoSkillMakerEntry() {
        override fun toString() = ""
    }

    object NextWave : AutoSkillMakerEntry() {
        override fun toString() = ",#,"
    }

    object NextTurn : AutoSkillMakerEntry() {
        override fun toString() = ","
    }
}