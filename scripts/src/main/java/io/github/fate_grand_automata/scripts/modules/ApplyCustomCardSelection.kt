package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.models.CustomCardSelectionPerTurn
import io.github.fate_grand_automata.scripts.models.ParsedCard
import io.github.lib_automata.dagger.ScriptScope
import javax.inject.Inject

@ScriptScope
class ApplyCustomCardSelection @Inject constructor(
    private val selection: CustomCardSelectionPerTurn
) {
    /**
     * Puts all selected cards in the front
     * Returns null if no selection found
     * Returns null if not all selected cards are available
     */
    fun pick(
        cards: List<ParsedCard>,
        turn: Int
    ): List<ParsedCard>? {
        val requirements = selection.atTurn(turn)
        if (requirements.isEmpty()) return null

        val available = cards.toMutableList()
        val picked = requirements.map { requirement ->
            available.find {
                it.fieldSlot == requirement.fieldSlot && it.type == requirement.type
            }?.also { available.remove(it) }
                ?: return null
        }

        return picked + available
    }
}
