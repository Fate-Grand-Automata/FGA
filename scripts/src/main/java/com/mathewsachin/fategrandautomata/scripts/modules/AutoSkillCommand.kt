package com.mathewsachin.fategrandautomata.scripts.modules

import com.mathewsachin.fategrandautomata.scripts.models.*
import com.mathewsachin.libautomata.ScriptExitException
import java.util.*

class AutoSkillCommand private constructor(
    private val stages: List<List<List<AutoSkillAction>>>
) {
    val lastStage = stages.lastIndex

    operator fun get(stage: Int, turn: Int): List<AutoSkillAction> {
        if (stage < stages.size) {
            val turns = stages[stage]

            if (turn < turns.size) {
                return turns[turn]
            }
        }

        return emptyList()
    }

    companion object {
        private fun getTarget(queue: Deque<Char>): ServantTarget? {
            val peekTarget = queue.peekFirst()
            val target = ServantTarget.list.firstOrNull { it.autoSkillCode == peekTarget }
            if (target != null) {
                queue.removeFirst()
            }

            return target
        }

        fun parseAction(queue: Deque<Char>): AutoSkillAction {
            try {
                return when (val c = queue.removeFirst()) {
                    in Skill.Servant.list.map { it.autoSkillCode } -> {
                        val skill = Skill.Servant.list.first { it.autoSkillCode == c }
                        val target = getTarget(queue)

                        AutoSkillAction.ServantSkill(skill, target)
                    }
                    in Skill.Master.list.map { it.autoSkillCode } -> {
                        val skill = Skill.Master.list.first { it.autoSkillCode == c }
                        val target = getTarget(queue)

                        AutoSkillAction.MasterSkill(skill, target)
                    }
                    in CommandCard.NP.list.map { it.autoSkillCode } -> {
                        val np = CommandCard.NP.list.first { it.autoSkillCode == c }

                        AutoSkillAction.NP(np)
                    }
                    't' -> {
                        val code = queue.removeFirst()
                        val target = EnemyTarget.list.first { it.autoSkillCode == code }
                        AutoSkillAction.TargetEnemy(target)
                    }
                    'n' -> {
                        val code = queue.removeFirst()
                        val count = code.toString().toInt()
                        AutoSkillAction.CardsBeforeNP(count)
                    }
                    'x' -> {
                        val startingCode = queue.removeFirst()
                        val starting = OrderChangeMember.Starting.list
                            .first { it.autoSkillCode == startingCode }

                        val subCode = queue.removeFirst()
                        val sub = OrderChangeMember.Sub.list
                            .first { it.autoSkillCode == subCode }

                        AutoSkillAction.OrderChange(starting, sub)
                    }
                    '0' -> AutoSkillAction.NoOp
                    else -> throw ScriptExitException("Unknown character: $c")
                }
            } catch (e: Exception) {
                throw ScriptExitException("AutoSkill Parse error:\n\n${e.message}")
            }
        }

        fun parse(command: String): AutoSkillCommand {
            val waves = command
                .split(",#,")

            val commandTable = waves
                .map {
                    val turns = it.split(',')
                    turns.map { cmd ->
                        val queue: Deque<Char> = ArrayDeque(cmd.length)
                        queue.addAll(cmd.asIterable())

                        val actions = mutableListOf<AutoSkillAction>()

                        while (!queue.isEmpty()) {
                            actions.add(parseAction(queue))
                        }

                        actions
                    }
                }

            return AutoSkillCommand(commandTable)
        }
    }
}