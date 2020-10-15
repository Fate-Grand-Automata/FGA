package com.mathewsachin.fategrandautomata.ui.skill_maker

import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillCommand

class SkillMakerModel(skillString: String) {
    private fun reduce(
        acc: List<SkillMakerEntry>,
        add: List<SkillMakerEntry>,
        separator: (AutoSkillAction.Atk) -> SkillMakerEntry.Next
    ): List<SkillMakerEntry> {
        if (acc.isNotEmpty()) {
            val last = acc.last()

            if (last is SkillMakerEntry.Action && last.action is AutoSkillAction.Atk) {
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
                    turn.map<AutoSkillAction, SkillMakerEntry> {
                        SkillMakerEntry.Action(it)
                    }
                }
                .reduce { acc, turn ->
                    reduce(acc, turn) { SkillMakerEntry.Next.Turn(it) }
                }
        }
        .reduce { acc, stage ->
            reduce(acc, stage) { SkillMakerEntry.Next.Wave(it) }
        }
        .let { listOf(SkillMakerEntry.Start) + it }
        .toMutableList()

    override fun toString(): String {
        fun getSkillCmd(): List<SkillMakerEntry> {
            if (skillCommand.isNotEmpty()) {
                val last = skillCommand.last()

                // remove trailing ',' or ',#,'
                if (last is SkillMakerEntry.Next) {
                    return skillCommand.subList(0, skillCommand.lastIndex) + SkillMakerEntry.Action(last.action)
                }
            }

            return skillCommand
        }

        return getSkillCmd().joinToString("")
    }
}