package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.IFgoAutomataApi
import io.github.fate_grand_automata.scripts.ScriptLog
import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle
import io.github.fate_grand_automata.scripts.models.battle.BattleState
import io.github.fate_grand_automata.scripts.prefs.IBattleConfig
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.math.max

@ScriptScope
class CommandTurnsTracker @Inject constructor(
    api: IFgoAutomataApi,
    private val state: BattleState,
    private val battleConfig: IBattleConfig,
    private val autoSkill: AutoSkill,
) : IFgoAutomataApi by api {

    private var outOfCommands = false

    fun trackTurns() {
        // It is already verified out of commands, no need to check further
        if (!outOfCommands) {
            trackSkipTurns()
        }

        isOffScript()

        outOfCommands = isOutOfCommand()
        if (outOfCommands && battleConfig.exitOnOutOfCommands) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.ExitOnOutOfCommands)
        }
    }


    /**
     * Track the number of turns to skip
     *
     * This will not run on the 1st stage, 1st turn
     *
     * commandTurnsUntilStage is the count of command turns from the start until the current stage.
     * Added additional turn since it is checking at next stage/wave
     *
     * Now perform the calculation to determine the number of turns to skip
     * If the number of turns to skip is greater than 0, add it to the current skip turns
     */
    private fun trackSkipTurns() {
        if (!(state.stage > 0 && state.turn < 1)) return

        var commandTurnsUntilStage = autoSkill.commandTurnsUntilStage(state.stage)

        // add additional turn since it is checking at next stage/wave
        commandTurnsUntilStage += 1

        val skipTurns = max(0, commandTurnsUntilStage - (state.currentTurn + state.skipCommandTurns))

        messages.log(
            ScriptLog.TurnTrackingAtNewStage(
                wave = state.stage,
                currentTurn = state.currentTurn,
                skipTurn = state.skipCommandTurns
            )
        )

        state.addSkipCommandTurns(skipTurns)
    }

    /**
     * Check if the script is off script
     *
     * This will not run on the 1st stage, 1st turn
     *
     * commandTurnsUntilStage is the count of command turns from the start until the current stage.
     * For example:
     *  - 1st wave has 1 command turn, commandTurnsUntilStage = 1
     *  - 2nd wave has 1 command turn, commandTurnsUntilStage = 2
     *  - 3rd wave has 2 command turns, commandTurnsUntilStage = 4
     *
     * If at the 2nd wave, which has 2 command turns, and you reach the next turn in the same wave (3 turns)
     * that would mean you are off script.
     */
    private fun isOffScript() {
        if (!battleConfig.exitOnOffScript) return

        // First Turn doesn't need to check
        if (state.stage == 0 && state.turn < 1) return

        val commandTurnsUntilStage = autoSkill.commandTurnsUntilStage(state.stage)

        val offScript = commandTurnsUntilStage < (state.currentTurn + state.skipCommandTurns)


        if (offScript) {
            throw AutoBattle.BattleExitException(AutoBattle.ExitReason.ExitOnOffScript)
        }

    }

    /**
     * Check if the script is out of commands
     *
     * Special case, when there are no commands, it is considered out of commands
     * Note: It is supposed to be 0, but idk why it is 1.
     * Probably because of the start flag that is getting drop
     * @see [SkillCommandGroup.kt]
     *
     * if the number of turns and skip turns is greater than the total command turns, it is considered out of commands
     */
    private fun isOutOfCommand(): Boolean {
        val totalCommandTurns = autoSkill.getTotalCommandTurns

        // If there are no commands, it is considered out of commands
        if (totalCommandTurns <= 1) return true

        return (state.currentTurn + state.skipCommandTurns) > totalCommandTurns
    }
}