package com.mathewsachin.fategrandautomata.ui.auto_skill_maker

import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillAction
import com.mathewsachin.fategrandautomata.scripts.models.AutoSkillCommand

class AutoSkillMakerModel(skillString: String) {
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
                    acc + AutoSkillMakerEntry.NextTurn + turn
                }
        }
        .reduce { acc, stage ->
            acc + AutoSkillMakerEntry.NextWave + stage
        }
        .let { listOf(AutoSkillMakerEntry.Start) + it }
        .toMutableList()

    override fun toString() = skillCommand.joinToString("")
}