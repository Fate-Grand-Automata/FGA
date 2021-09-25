package com.mathewsachin.fategrandautomata.scripts.supportSelection

import com.mathewsachin.fategrandautomata.scripts.entrypoints.AutoBattle

object ManualSupportSelection: SupportSelectionProvider {
    override fun select() = throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionManual)
}