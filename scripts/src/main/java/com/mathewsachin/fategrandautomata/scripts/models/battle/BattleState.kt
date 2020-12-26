package com.mathewsachin.fategrandautomata.scripts.models.battle

import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.TimeSource

class BattleState {
    private var timestamp = TimeSource.Monotonic.markNow()
    fun markStartTime() {
        timestamp = TimeSource.Monotonic.markNow()
    }

    val totalBattleTime get() = timestamp.elapsedNow()

    var maxRunTime = Duration.ZERO
        private set
    var minRunTime = Duration.INFINITE
        private set
    var averageRunTime = Duration.ZERO
        private set

    var totalTurns = 0
        private set
    var maxTurns = 0
        private set
    var minTurns = Int.MAX_VALUE
        private set
    var averageTurns = 0
        private set

    var runs = 0
        private set(value) {
            field = value

            val runTime = runState.runTime

            maxRunTime = maxOf(runTime, maxRunTime)
            minRunTime = minOf(runTime, minRunTime)
            averageRunTime = totalBattleTime / runs

            totalTurns += runState.totalTurns
            maxTurns = maxOf(maxTurns, runState.totalTurns)
            minTurns = minOf(minTurns, runState.totalTurns)
            averageTurns = (totalTurns / runs.toDouble()).roundToInt()

            runState = RunState()
        }

    private var runState = RunState()

    var hasChosenTarget
        get() = runState.stageState.hasChosenTarget
        set(value) {
            runState.stageState.hasChosenTarget = value
        }

    var hasClickedAttack
        get() = runState.turnState.hasClickedAttack
        set(value) {
            runState.turnState.hasClickedAttack = value
        }

    var stageCountSnaphot
        get() = runState.stageState.stageCountSnaphot
        set(value) {
            runState.stageState.stageCountSnaphot = value
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