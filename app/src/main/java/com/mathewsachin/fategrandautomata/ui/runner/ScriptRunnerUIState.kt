package com.mathewsachin.fategrandautomata.ui.runner

sealed class ScriptRunnerUIState {
    object Running: ScriptRunnerUIState()
    class Paused(val pausedStatus: Exception?): ScriptRunnerUIState()
    object Idle: ScriptRunnerUIState()
}