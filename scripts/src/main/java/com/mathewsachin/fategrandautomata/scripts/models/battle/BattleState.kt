package com.mathewsachin.fategrandautomata.scripts.models.battle

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

    var runs = 0
        private set(value) {
            field = value

            val runTime = runState.runTime

            maxRunTime = maxOf(runTime, maxRunTime)
            minRunTime = minOf(runTime, minRunTime)
            averageRunTime = totalBattleTime / runs

            runState = RunState()
        }

    var runState = RunState()
        private set

    fun nextRun() = ++runs
}