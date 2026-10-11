package io.github.fate_grand_automata.scripts.modules.attack

import io.github.fate_grand_automata.scripts.enums.CardTypeEnum
import io.github.fate_grand_automata.scripts.models.ParsedCard

object AttackUtils {
    fun getValidNonUnknownCards(cards: List<ParsedCard>): List<ParsedCard> =
        cards.filter { it.type != CardTypeEnum.Unknown }
}
