package io.github.fate_grand_automata.scripts.supportSelection

import io.github.fate_grand_automata.scripts.entrypoints.AutoBattle

object ManualSupportSelection : SupportSelectionProvider {
    override fun select() = throw AutoBattle.BattleExitException(AutoBattle.ExitReason.SupportSelectionManual)
}
