package io.github.fate_grand_automata.ui.skill_maker

import io.github.fate_grand_automata.scripts.models.AutoSkillAction
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.Skill

sealed class SkillMakerEntry {
    class Action(val action: AutoSkillAction) : SkillMakerEntry() {
        private fun toString(skill: Skill, target: ServantTarget?) =
            if (target == null)
                "${skill.autoSkillCode}"
            else "${skill.autoSkillCode}${target.autoSkillCode}"

        private fun toString(skill: Skill, targets: List<ServantTarget>) =
            if (targets.isEmpty()) "${skill.autoSkillCode}"
            else if (targets.size == 1) "${skill.autoSkillCode}${targets[0].autoSkillCode}"
            else "${skill.autoSkillCode}(${targets.map(ServantTarget::autoSkillCode).joinToString("")})"

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

            is AutoSkillAction.ServantSkill -> toString(action.skill, action.targets)
            is AutoSkillAction.MasterSkill -> toString(action.skill, action.target)
            is AutoSkillAction.TargetEnemy -> "t${action.enemy.autoSkillCode}"
            is AutoSkillAction.OrderChange -> "x${action.starting.autoSkillCode}${action.sub.autoSkillCode}"
        }
    }

    object Start : SkillMakerEntry() {
        override fun toString() = ""
    }

    sealed class Next(val action: AutoSkillAction.Atk) : SkillMakerEntry() {
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