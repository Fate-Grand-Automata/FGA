package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import java.util.ArrayDeque
import java.util.Deque
import java.util.Queue

class AutoSkillCommand private constructor(
    val stages: List<List<List<AutoSkillAction>>>
) {
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

        // Prepare the list of special targets
        private val specialTargetList = ServantTarget
            .list
            .filter {
                it.specialTarget.isNotEmpty()
            }

        private fun getTarget(queue: Queue<Char>): ServantTarget? {
            val peekTarget = queue.peek()
            var target: ServantTarget? = null
            if (peekTarget == ServantTarget.SpecialTarget.startChar()) {
                // remove initial [
                queue.remove()

                var special = ""
                var char: Char? = null

                while (queue.isNotEmpty()) {
                    char = queue.remove()
                    if (char == ServantTarget.SpecialTarget.endChar()) break

                    if (char != ServantTarget.SpecialTarget.startChar()) {
                        special += char
                    }
                    target = specialTargetList
                        .firstOrNull {
                            it.specialTarget == special
                        }
                }
                if (char != ServantTarget.SpecialTarget.endChar()) {
                    throw Exception("Found [ but no matching ] in Skill Command")
                }
                if (special.isEmpty()) {
                    throw Exception("Command Can't be empty")
                }
                if (target == null) {
                    throw Exception("Special target \"$special\" not found")
                }
            } else {
                target = ServantTarget.list.firstOrNull { it.autoSkillCode == peekTarget }
                if (target != null) {
                    queue.remove()
                }
            }
            return target
        }

        private fun getTargets(queue: Queue<Char>): List<ServantTarget> {
            val targets = mutableListOf<ServantTarget>()
            val nextChar = queue.peek()
            if (nextChar == ServantTarget.multiTargetStartChar()) {
                queue.remove()
                var char: Char? = null
                var specialFound = false
                var special = ""
                while (queue.isNotEmpty()) {
                    char = queue.remove()
                    if (char == ServantTarget.multiTargetEndChar()) break

                    if (char == ServantTarget.SpecialTarget.startChar()) {
                        specialFound = true
                    } else if (char == ServantTarget.SpecialTarget.endChar()) {
                        specialFound = false
                        val target = specialTargetList.firstOrNull {
                            it.specialTarget == special
                        }
                        target?.let {
                            targets.add(it)

                            // reset
                            special = ""
                        } ?: run {
                            if (special.isEmpty()) {
                                throw Exception("Command Can't be empty")
                            } else {
                                throw Exception("Special target \"$special\" not found")
                            }
                        }
                    }

                    if (specialFound) {
                        if (char != ServantTarget.SpecialTarget.startChar()) {
                            special += char
                        }
                    } else {
                        val target = ServantTarget.list.firstOrNull { it.autoSkillCode == char }
                        target?.let(targets::add)
                    }
                }
                if (char != ServantTarget.multiTargetEndChar()) {
                    throw Exception("Found ( but no matching ) in Skill Command")
                }
                if (specialFound) {
                    throw Exception("Found [ but no matching ] in Skill Command")
                }
            } else {
                getTarget(queue)?.let(targets::add)
            }

            return targets
        }

        private fun parseAction(queue: Queue<Char>): AutoSkillAction {
            try {
                return when (val c = queue.remove()) {
                    in Skill.Servant.list.map { it.autoSkillCode } -> {
                        val skill = Skill.Servant.list.first { it.autoSkillCode == c }
                        val targets = getTargets(queue)

                        AutoSkillAction.ServantSkill(skill, targets)
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

                    SpecialCommand.EnemyTarget.autoSkillCode -> {
                        val code = queue.remove()
                        val target = EnemyTarget.list.first { it.autoSkillCode == code }
                        AutoSkillAction.TargetEnemy(target)
                    }

                    SpecialCommand.CardsBeforeNP.autoSkillCode -> {
                        val code = queue.remove()
                        val count = code.toString().toInt()
                        AutoSkillAction.Atk.cardsBeforeNP(count)
                    }

                    SpecialCommand.OrderChange.autoSkillCode -> {
                        val startingCode = queue.remove()
                        val starting = OrderChangeMember.Starting.list
                            .first { it.autoSkillCode == startingCode }

                        val subCode = queue.remove()
                        val sub = OrderChangeMember.Sub.list
                            .first { it.autoSkillCode == subCode }

                        AutoSkillAction.OrderChange(starting, sub)
                    }

                    SpecialCommand.NoOp.autoSkillCode -> AutoSkillAction.Atk.noOp()

                    else -> throw Exception("Unknown character: $c")
                }
            } catch (e: Exception) {
                throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SkillCommandParseError(e))
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

                        while (queue.isNotEmpty()) {
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