package io.github.fate_grand_automata.ui.skill_maker

import io.github.fate_grand_automata.scripts.models.AutoSkillAction
import io.github.fate_grand_automata.scripts.models.ServantTarget
import io.github.fate_grand_automata.scripts.models.Skill
import io.github.fate_grand_automata.scripts.models.SpecialCommand

sealed class SkillMakerEntry {
    class Action(val action: AutoSkillAction) : SkillMakerEntry() {
        private fun toString(skill: Skill, target: ServantTarget?) = when (target) {
            null -> "${skill.autoSkillCode}"
            else -> {
                if (target.specialTarget.isNotEmpty()) {
                    "${skill.autoSkillCode}${target.autoSkillCode}${target.specialTarget}${SpecialCommand.EndSpecialTarget.autoSkillCode}"
                } else {
                    "${skill.autoSkillCode}${target.autoSkillCode}"
                }
            }
        }

        private fun toString(skill: Skill, targets: List<ServantTarget>) = when {
            targets.isEmpty() -> "${skill.autoSkillCode}"
            targets.size == 1 -> {
                if (targets[0].specialTarget.isNotEmpty()) {
                    "${skill.autoSkillCode}${targets[0].autoSkillCode}${targets[0].specialTarget}${SpecialCommand.EndSpecialTarget.autoSkillCode}"
                } else {
                    "${skill.autoSkillCode}${targets[0].autoSkillCode}"
                }
            }

            else -> {
                val start = "${skill.autoSkillCode}${SpecialCommand.StartMultiTarget.autoSkillCode}"
                val end = "${SpecialCommand.EndMultiTarget.autoSkillCode}"

                val middle = targets.joinToString("") { target ->
                    if (target.specialTarget.isNotEmpty()) {
                        "${target.autoSkillCode}${target.specialTarget}${SpecialCommand.EndSpecialTarget.autoSkillCode}"
                    } else {
                        "${target.autoSkillCode}"
                    }
                }

                start + middle + end
            }
        }

        override fun toString() = when (action) {
            is AutoSkillAction.Atk -> {
                if (action == AutoSkillAction.Atk.noOp()) {
                    "0"
                } else {
                    val cardsBeforeNP = if (action.cardsBeforeNP > 0) {
                        "${SpecialCommand.CardsBeforeNP.autoSkillCode}${action.cardsBeforeNP}"
                    } else ""

                    cardsBeforeNP + action.nps.joinToString("") {
                        it.autoSkillCode.toString()
                    }
                }
            }

            is AutoSkillAction.ServantSkill -> toString(action.skill, action.targets)
            is AutoSkillAction.MasterSkill -> toString(action.skill, action.target)
            is AutoSkillAction.TargetEnemy -> "${SpecialCommand.EnemyTarget.autoSkillCode}${action.enemy.autoSkillCode}"
            is AutoSkillAction.OrderChange -> "${SpecialCommand.OrderChange.autoSkillCode}${action.starting.autoSkillCode}${action.sub.autoSkillCode}"
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