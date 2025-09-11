package io.github.fate_grand_automata.scripts.models.battle

import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.TimeSource

@ScriptScope
class BattleState @Inject constructor() {
    private var timestamp = TimeSource.Monotonic.markNow()
    fun markStartTime() {
        timestamp = TimeSource.Monotonic.markNow()
    }

    val totalBattleTime get() = timestamp.elapsedNow()

    var maxTimePerRun = Duration.ZERO
        private set
    var minTimePerRun = Duration.INFINITE
        private set
    var averageTimePerRun = Duration.ZERO
        private set

    private var totalTurns = 0
    var maxTurnsPerRun = 0
        private set
    var minTurnsPerRun = Int.MAX_VALUE
        private set
    var averageTurnsPerRun: Double = 0.0
        private set

    var runs = 0
        private set(value) {
            field = value

            val runTime = runState.runTime

            maxTimePerRun = maxOf(runTime, maxTimePerRun)
            minTimePerRun = minOf(runTime, minTimePerRun)
            averageTimePerRun = totalBattleTime / runs

            totalTurns += runState.totalTurns
            maxTurnsPerRun = maxOf(maxTurnsPerRun, runState.totalTurns)
            minTurnsPerRun = minOf(minTurnsPerRun, runState.totalTurns)
            averageTurnsPerRun = totalTurns / runs.toDouble()

            runState = RunState()
        }

    private var runState = RunState()

    var chosenTarget
        get() = runState.stageState.chosenTarget
        set(value) {
            runState.stageState.chosenTarget = value
        }

    var stageCountSnapshot
        get() = runState.stageState.stageCountSnapshot
        set(value) {
            runState.stageState.stageCountSnapshot = value
        }

    var shuffled
        get() = runState.shuffled
        set(value) {
            runState.shuffled = value
        }

    val stage get() = runState.stage
    val turn get() = runState.turn

    fun nextTurn() = runState.nextTurn()
    fun nextStage() = runState.nextStage()

    fun nextRun() = ++runs
}
