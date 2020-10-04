package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillCommand

class AutoSkillMakerModel(skillString: String) {
    private fun reduce(
        acc: List<AutoSkillMakerEntry>,
        add: List<AutoSkillMakerEntry>,
        separator: (AutoSkillAction.Atk) -> AutoSkillMakerEntry.Next
    ): List<AutoSkillMakerEntry> {
        if (acc.isNotEmpty()) {
            val last = acc.last()

            if (last is AutoSkillMakerEntry.Action && last.action is AutoSkillAction.Atk) {
                return acc.subList(0, acc.lastIndex) + separator(last.action) + add
            }
        }

        return acc + separator(AutoSkillAction.Atk.noOp()) + add
    }

    val skillCommand = AutoSkillCommand.parse(skillString)
        .stages
        .map { turns ->
            turns
                .map { turn ->
                    turn.map<AutoSkillAction, AutoSkillMakerEntry> {
                        AutoSkillMakerEntry.Action(it)
                    }
                }
                .reduce { acc, turn ->
                    reduce(acc, turn) { AutoSkillMakerEntry.Next.Turn(it) }
                }
        }
        .reduce { acc, stage ->
            reduce(acc, stage) { AutoSkillMakerEntry.Next.Wave(it) }
        }
        .let { listOf(AutoSkillMakerEntry.Start) + it }
        .toMutableList()

    override fun toString(): String {
        fun getSkillCmd(): List<AutoSkillMakerEntry> {
            if (skillCommand.isNotEmpty()) {
                val last = skillCommand.last()

                // remove trailing ',' or ',#,'
                if (last is AutoSkillMakerEntry.Next) {
                    return skillCommand.subList(0, skillCommand.lastIndex) + AutoSkillMakerEntry.Action(last.action)
                }
            }

            return skillCommand
        }

        return getSkillCmd().joinToString("")
    }
}