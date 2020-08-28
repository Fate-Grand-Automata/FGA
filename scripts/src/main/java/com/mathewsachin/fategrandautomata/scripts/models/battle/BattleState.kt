package com.mathewsachin.fategrandautomata.scripts.models.battle

class BattleState {
    var runs = 0
        private set(value) {
            field = value
            runState = RunState()
        }

    var runState = RunState()
        private set

    fun nextRun() = ++runs
}