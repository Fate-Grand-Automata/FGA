package io.github.fate_grand_automata.scripts.models

import io.github.fate_grand_automata.scripts.enums.CardAffinityEnum
import io.github.fate_grand_automata.scripts.enums.CardTypeEnum

data class ParsedCard(
    val card: CommandCard.Face,
    val servant: TeamSlot,
    val fieldSlot: FieldSlot?,
    val type: CardTypeEnum,
    val affinity: CardAffinityEnum = CardAffinityEnum.Normal,
    val isStunned: Boolean = false
) {
    override fun equals(other: Any?) =
        other is ParsedCard && card == other.card

    override fun hashCode() = card.hashCode()
}