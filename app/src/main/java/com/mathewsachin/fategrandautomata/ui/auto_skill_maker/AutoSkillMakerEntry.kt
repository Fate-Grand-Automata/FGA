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
            is AutoSkillAction.Atk -> {
                if (action == AutoSkillAction.Atk.noOp()) {
                    "0"
                } else {
                    val cardsBeforeNP = if (action.cardsBeforeNP > 0) {
                        "n${action.cardsBeforeNP}"
                    } else ""

                    cardsBeforeNP + action.nps.joinToString("") {
                        it.autoSkillCode.toString()
                    }
                }
            }
            is AutoSkillAction.ServantSkill -> toString(action.skill, action.target)
            is AutoSkillAction.MasterSkill -> toString(action.skill, action.target)
            is AutoSkillAction.TargetEnemy -> "t${action.enemy.autoSkillCode}"
            is AutoSkillAction.OrderChange -> "x${action.starting.autoSkillCode}${action.sub.autoSkillCode}"
        }
    }

    object Start : AutoSkillMakerEntry() {
        override fun toString() = ""
    }

    sealed class Next(val action: AutoSkillAction.Atk) : AutoSkillMakerEntry() {
        protected fun AutoSkillAction.Atk.str() = if (action == AutoSkillAction.Atk.noOp()) ""
        else Action(this).toString()

        class Wave(action: AutoSkillAction.Atk) : Next(action) {
            override fun toString() = "${action.str()},#,"
        }

        class Turn(action: AutoSkillAction.Atk) : Next(action) {
            override fun toString() = "${action.str()},"
        }
    }
}