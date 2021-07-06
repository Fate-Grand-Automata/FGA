package com.mathewsachin.fategrandautomata.scripts

import com.mathewsachin.fategrandautomata.scripts.models.CommandCard
import kotlin.time.Duration

sealed class ScriptNotify {
    object CEGet: ScriptNotify()
    object CEDropped: ScriptNotify()
    class WaitForAPRegen(val minutes: Int = 1): ScriptNotify()
    class FailedToDetermineCards(val cards: List<CommandCard.Face>): ScriptNotify()
    class SupportListUpdatingIn(val time: Duration): ScriptNotify()
    class BetweenRuns(val refills: Int, val runs: Int): ScriptNotify()
}

interface IScriptMessages {
    fun notify(action: ScriptNotify)
}