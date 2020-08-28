package com.mathewsachin.fategrandautomata.scripts.models.battle

class RunState {
    var stage = -1
        private set(value) {
            field = value
            stageState.stageCountSnaphot?.close()
            stageState = StageState()
            turn = -1
        }

    var stageState = StageState()
        private set

    var turn = -1
        private set(value) {
            field = value
            turnState = TurnState()
        }

    var turnState = TurnState()
        private set

    fun nextStage() = ++stage

    fun nextTurn() = ++turn
}