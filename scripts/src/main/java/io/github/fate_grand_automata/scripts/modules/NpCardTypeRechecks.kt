package io.github.fate_grand_automata.scripts.modules

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.TeamSlot

internal fun confirmedFormNpCardType(readType: CardTypeEnum?, formType: CardTypeEnum?): CardTypeEnum? =
    readType?.takeIf { formType == null || it == formType }

/** A digits-only crop must yield a single positive decimal number to justify delaying a read. */
internal fun parseNpChangeTurns(text: String): Int? {
    val digits = text.trim()
    if (digits.any { !it.isDigit() }) return null
    return digits.toIntOrNull()?.takeIf { it > 0 }
}

/** Counts turn starts per pending Servant independently of their position on the field. */
internal class NpCardTypeRechecks {
    private val turnsUntilRead = mutableMapOf<TeamSlot, Int>()

    fun schedule(slot: TeamSlot, turns: Int) {
        turnsUntilRead[slot] = turns
    }

    fun beginTurn() {
        // One remaining turn includes the turn showing the banner. A benched Servant stays due
        // once that interval passes, so Order Change can check them on their return.
        turnsUntilRead.replaceAll { _, turns -> (turns - 1).coerceAtLeast(0) }
    }

    fun isDue(slot: TeamSlot) = turnsUntilRead[slot] == 0

    fun remove(slot: TeamSlot) {
        turnsUntilRead.remove(slot)
    }

    fun clear() = turnsUntilRead.clear()
}
