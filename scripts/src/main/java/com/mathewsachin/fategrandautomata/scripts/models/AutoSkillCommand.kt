package com.mathewsachin.fategrandautomata.scripts.models

import com.mathewsachin.libautomata.ScriptExitException
import java.util.*

class AutoSkillCommand private constructor(
    val stages: List<List<List<AutoSkillAction>>>
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
        private fun getTarget(queue: Queue<Char>): ServantTarget? {
            val peekTarget = queue.peek()
            val target = ServantTarget.list.firstOrNull { it.autoSkillCode == peekTarget }
            if (target != null) {
                queue.remove()
            }

            return target
        }

        private fun parseAction(queue: Queue<Char>): AutoSkillAction {
            try {
                return when (val c = queue.remove()) {
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

                        AutoSkillAction.Atk.np(np)
                    }
                    't' -> {
                        val code = queue.remove()
                        val target = EnemyTarget.list.first { it.autoSkillCode == code }
                        AutoSkillAction.TargetEnemy(target)
                    }
                    'n' -> {
                        val code = queue.remove()
                        val count = code.toString().toInt()
                        AutoSkillAction.Atk.cardsBeforeNP(count)
                    }
                    'x' -> {
                        val startingCode = queue.remove()
                        val starting = OrderChangeMember.Starting.list
                            .first { it.autoSkillCode == startingCode }

                        val subCode = queue.remove()
                        val sub = OrderChangeMember.Sub.list
                            .first { it.autoSkillCode == subCode }

                        AutoSkillAction.OrderChange(starting, sub)
                    }
                    '0' -> AutoSkillAction.Atk.noOp()
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
                            val action = parseAction(queue)

                            // merge NPs and cards before NPs
                            if (actions.isNotEmpty() && action is AutoSkillAction.Atk) {
                                val last = actions.last()

                                if (last is AutoSkillAction.Atk) {
                                    actions[actions.lastIndex] = last + action

                                    continue
                                }
                            }

                            actions.add(action)
                        }

                        actions
                    }
                }

            return AutoSkillCommand(commandTable)
        }
    }
}